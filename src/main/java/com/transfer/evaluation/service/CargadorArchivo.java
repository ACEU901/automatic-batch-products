package com.transfer.evaluation.service;

import com.transfer.evaluation.service.dao.Conexion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class CargadorArchivo {

    private static final int LOTE_INICIAL = 1000;  // Lote inicial
    private static final int LOTE_INCREMENTO = 2000;  // Incremento del lote

    public static void cargarArchivo(File archivo, JProgressBar barraProgresoDb) {
        List<String[]> lineas = new ArrayList<>();
        boolean esExcel = archivo.getName().endsWith(".xlsx") || archivo.getName().endsWith(".xls");

        // 1. Leer todas las líneas del archivo según el tipo
        try {
            if (esExcel) {
                try (FileInputStream fis = new FileInputStream(archivo);
                     Workbook workbook = new XSSFWorkbook(fis)) {
                    Sheet sheet = workbook.getSheetAt(0);
                    for (Row row : sheet) {
                        String[] datos = new String[7];
                        for (int i = 0; i < 7; i++) {
                            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            datos[i] = cell.toString();
                        }
                        lineas.add(datos);
                    }
                }
            } else {
                try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        lineas.add(linea.split(";"));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            return;
        }

        // 2. Conexión a la base de datos
        Connection conexion = Conexion.obtenerConexion();

        if (conexion == null) {
            System.err.println("No se pudo establecer la conexión con la base de datos.");
            return;
        }

        String sql = "INSERT INTO PRODUCTO (NumSuc, Sku, CODBARRA, Descripcion, Fam, SalFisSuc, Valor) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            int registrosInsertados = 0;
            int cantidadRegistros = lineas.size();
            int batchSize = LOTE_INICIAL;

            // Establecer el valor mínimo y máximo para la barra de progreso
            barraProgresoDb.setMinimum(0);
            barraProgresoDb.setMaximum(cantidadRegistros);

            for (int i = 0; i < cantidadRegistros; i++) {
                boolean rendimientoAceptable = validPerformance();
                if (!rendimientoAceptable) {
                    batchSize = LOTE_INICIAL;
                }

                String[] datos = lineas.get(i);

                if (datos.length != 7) {
                    System.err.println("Línea inválida en la posición " + (i + 1));
                    continue;
                }

                setPreparedStatement(statement, datos);
                statement.addBatch();

                if ((i + 1) % batchSize == 0 || (i + 1) == cantidadRegistros) {
                    statement.executeBatch();
                    registrosInsertados += batchSize;
                    System.out.println("Registros insertados: " + registrosInsertados);

                    if (rendimientoAceptable) {
                        batchSize = Math.min(batchSize + LOTE_INCREMENTO, 500000);
                    }
                }
                barraProgresoDb.setValue(i + 1);
            }

            System.out.println("Carga completada. Registros insertados: " + registrosInsertados);

        } catch (SQLException e) {
            System.err.println("Error al insertar en la base de datos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Conexion.cerrarConexion();
        }
    }

    // Método que ajusta el PreparedStatement con los valores de la línea
    private static void setPreparedStatement(PreparedStatement statement, String[] datos) throws SQLException {
        float numSuc = Float.parseFloat(datos[0].trim());
        String sku = datos[1].trim();
        String codBarra = datos[2].trim();
        String descripcion = datos[3].trim();
        float fam = Float.parseFloat(datos[4].trim());
        float salFisSuc = Float.parseFloat(datos[5].trim());
        float valor = Float.parseFloat(datos[6].trim().replace(",", "."));

        statement.setFloat(1, numSuc);
        statement.setString(2, sku);
        statement.setString(3, codBarra);
        statement.setString(4, descripcion);
        statement.setFloat(5, fam);
        statement.setFloat(6, salFisSuc);
        statement.setFloat(7, valor);
    }

    // Método para verificar el rendimiento y determinar si se puede aumentar el tamaño del lote
    private static boolean validPerformance() {
        try {
            double cpu = SystemMonitor.getCpuUsagePercent();
            double ram = SystemMonitor.getMemoryUsagePercent();
            long latencia = SystemMonitor.getInternetLatencyMs();

            // Si CPU o RAM superan el 80%, reducir el lote
            if (cpu > 80 || ram > 80) {
                System.out.println("Rendimiento bajo. Reduciendo tamaño del lote a 1000.");
                return false;
            }

            // Si la latencia supera los 900ms, reducir el lote y hacer pausa
            if (latencia > 900) {
                System.out.println("Alta latencia. Durmiendo 3s...");
                Thread.sleep(3_000);
                return false;
            }

            // Si todo está bien, permitir el aumento del lote
            return true;

        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
