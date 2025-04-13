package com.transfer.evaluation.service;

import com.transfer.evaluation.service.db.Conexion;

import javax.swing.*;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import static com.transfer.evaluation.funcion.DbValidation.readLines;
import static com.transfer.evaluation.funcion.Performance.validPerformanceByDb;

public class CargadorArchivo {

    private static final int LOTE_INICIAL = 5000;

    public static void cargarArchivo(File archivo, JProgressBar barraProgresoDb) {
        List<String[]> lineas = new ArrayList<>();
        boolean esExcel = archivo.getName().endsWith(".xlsx") || archivo.getName().endsWith(".xls");
        boolean continuar = true;
        // 1. Leer todas las líneas del archivo según el tipo
        continuar = readLines(lineas, esExcel, archivo);
        if (!continuar) {
            return;
        }
        // 2. Conexión a la base de datos
        insertExecute(lineas, barraProgresoDb);
    }

    private static void insertExecute(List<String[]> lineas, JProgressBar barraProgresoDb) {
        Connection conexion = Conexion.obtenerConexion();

        if (conexion == null) {
            System.err.println("No se pudo establecer la conexión con la base de datos.");
        }
        String sql = "INSERT INTO PRODUCTO (NumSuc, Sku, CODBARRA, Descripcion, Fam, SalFisSuc, Valor) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            int registrosInsertados = 0;
            int cantidadRegistros = lineas.size();
            Integer batchSize = LOTE_INICIAL;

            // Establecer el valor mínimo y máximo para la barra de progreso
            barraProgresoDb.setMinimum(0);
            barraProgresoDb.setMaximum(cantidadRegistros);
            for (int i = 0; i < cantidadRegistros; i++) {
                if (i % batchSize == 0) {
                    batchSize = validPerformanceByDb(batchSize);
                }

                String[] datos = lineas.get(i);

                if (datos.length != 7) {
                    System.err.println("Línea inválida en la posición " + (i + 1));
                    continue;
                }

                setPreparedStatement(statement, datos);
                statement.addBatch();

                if ((i + 1) % batchSize == 0 || (i + 1) == cantidadRegistros) {
                    int[] resultados = statement.executeBatch();
                    registrosInsertados += resultados.length;
                    System.out.println("Registros insertados: " + resultados.length);
                }
                barraProgresoDb.setValue(i + 1);
            }

            System.out.println("Carga completada. Registros insertados: " + registrosInsertados);

        } catch (SQLException e) {
            System.err.println("Error al insertar en la base de datos: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            Conexion.cerrarConexion();
        }
    }

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

}
