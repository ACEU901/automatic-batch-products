package com.transfer.evaluation.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ExcelTransferService {

    public static File copiarArchivo(File archivoOriginal) throws IOException {
        Path destino = Paths.get("archivos_cargados", archivoOriginal.getName());
        Files.createDirectories(destino.getParent());
        return Files.copy(archivoOriginal.toPath(), destino, StandardCopyOption.REPLACE_EXISTING).toFile();
    }

    public static TransferResult procesarArchivo(File archivoEntrada, JProgressBar barraProgreso) throws IOException, InterruptedException {
        String nombre = archivoEntrada.getName().toLowerCase();

        if (nombre.endsWith(".xlsx") || nombre.endsWith(".xls")) {
            return procesarExcel(archivoEntrada, barraProgreso);
        } else if (nombre.endsWith(".csv")) {
            return procesarCSV(archivoEntrada, barraProgreso);
        } else {
            throw new IllegalArgumentException("Formato de archivo no soportado.");
        }
    }

    // ---------------- Excel ----------------

    private static TransferResult procesarExcel(File archivo, JProgressBar barraProgreso) throws IOException, InterruptedException {
        List<Row> registrosCompletos = new ArrayList<>();
        int totalLeidos = 0;

        try (FileInputStream fis = new FileInputStream(archivo);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet hoja = workbook.getSheetAt(0);
            int total = hoja.getLastRowNum();
            barraProgreso.setMaximum(total);
            barraProgreso.setValue(0);

            for (int i = 1; i <= total; i++) {
                validPerformance();
                //aplicar logica de 1000 incrementales y validacion de rendimiento actual
                Row fila = hoja.getRow(i);
                totalLeidos++;
                if (esRegistroCompleto(fila)) {
                    registrosCompletos.add(fila);
                }
                barraProgreso.setValue(i);
            }

            int cantidad = registrosCompletos.size();
            escribirNuevoExcel(registrosCompletos.subList(0, cantidad), hoja.getRow(0));

            return new TransferResult(totalLeidos, cantidad);
        }
    }

    private static boolean esRegistroCompleto(Row fila) {
        if (fila == null) return false;
        for (Cell celda : fila) {
            if (celda == null || celda.getCellType() == CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private static void escribirNuevoExcel(List<Row> registros, Row encabezado) throws IOException {
        Workbook nuevoWorkbook = new XSSFWorkbook();
        Sheet hoja = nuevoWorkbook.createSheet("Procesados");

        Row nuevaCabecera = hoja.createRow(0);
        for (int i = 0; i < encabezado.getLastCellNum(); i++) {
            Cell original = encabezado.getCell(i);
            Cell nuevo = nuevaCabecera.createCell(i);
            if (original != null) nuevo.setCellValue(original.toString());
        }

        Path destino = Paths.get("archivos_procesados", "procesados_excel_" + System.currentTimeMillis() + ".xlsx");
        Files.createDirectories(destino.getParent());
        try (FileOutputStream fos = new FileOutputStream(destino.toFile())) {
            nuevoWorkbook.write(fos);
        }
        nuevoWorkbook.close();
    }

    // ---------------- CSV ----------------

    private static TransferResult procesarCSV(File archivo, JProgressBar barraProgreso) throws IOException {
        List<String[]> registrosCompletos = new ArrayList<>();
        String[] encabezado = null;
        int totalLeidos = 0;

        List<String> lineas = Files.readAllLines(archivo.toPath());
        barraProgreso.setMaximum(lineas.size());
        barraProgreso.setValue(0);

        for (int i = 0; i < lineas.size(); i++) {
            try {
                validPerformance();
                String lineaCruda = lineas.get(i);
                String linea = lineaCruda.trim();

                if (linea.isEmpty() || !linea.contains(",")) continue;
                if (i == 0) linea = linea.replace("\uFEFF", "");

                String[] columnas = linea.split(",");
                if (columnas.length < 2) continue;

                if (encabezado == null) {
                    encabezado = columnas;
                } else {
                    totalLeidos++;
                    if (esRegistroCompleto(columnas)) {
                        registrosCompletos.add(columnas);
                    }
                }

                barraProgreso.setValue(i + 1);

            } catch (Exception ex) {
                System.err.println("Error al procesar línea " + (i + 1) + ": " + ex.getMessage());
            }
        }

        if (encabezado == null) {
            throw new IOException("Encabezado del archivo CSV no detectado correctamente.");
        }

        int cantidad = registrosCompletos.size();
        escribirNuevoCSV(registrosCompletos.subList(0, cantidad), encabezado);

        return new TransferResult(totalLeidos, cantidad);
    }

    private static boolean esRegistroCompleto(String[] columnas) {
        for (String dato : columnas) {
            if (dato == null || dato.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void escribirNuevoCSV(List<String[]> registros, String[] encabezado) throws IOException {
        if (encabezado == null) {
            throw new IOException("No se puede escribir CSV: el encabezado es nulo.");
        }

        Path destino = Paths.get("archivos_procesados", "procesados_csv_" + System.currentTimeMillis() + ".csv");
        Files.createDirectories(destino.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(destino)) {
            writer.write(String.join(",", encabezado));
            writer.newLine();

            for (String[] fila : registros) {
                writer.write(String.join(",", fila));
                writer.newLine();
            }
        }
    }

    private static void validPerformance() throws InterruptedException {
        double cpu = SystemMonitor.getCpuUsagePercent();
        double ram = SystemMonitor.getMemoryUsagePercent();
        long latencia = SystemMonitor.getInternetLatencyMs();


        if (cpu > 90 || ram > 90) {
            System.out.println("CPU o RAM > 90%.");
            Thread.sleep(10_000);
        } else if (cpu > 80 || ram > 80) {
            System.out.println("CPU o RAM > 80%. Durmiendo 1s...");
            Thread.sleep(10_000);

        } else if (latencia > 900) {
            System.out.println("Alta latencia (" + latencia + "ms). Durmiendo 3s...");
            Thread.sleep(3_000);
        }
    }

    public static void iniciarProcesamientoBatch(List<?> registros, boolean esExcel) throws IOException, InterruptedException {
        int start = 0;
        int batchSize = 1000; // Tamaño inicial del lote
        int incremento = 2000; // Incremento de tamaño del lote
        int total = registros.size();

        while (start < total) {
            // Monitorear el uso de recursos del sistema
            double cpu = SystemMonitor.getCpuUsagePercent();
            double ram = SystemMonitor.getMemoryUsagePercent();
            long latencia = SystemMonitor.getInternetLatencyMs();

            // 4.1 Validar Memoria RAM, CPU, INTERNET
            // Ajuste dinámico del tamaño del lote basado en el uso de recursos
            if (cpu > 90 || ram > 90) {
                System.out.println("CPU o RAM > 90%. Pausando 10s...");
                Thread.sleep(10_000); // 4.3.1
                batchSize = 1000; // Reducir a 1000 registros
            } else if (cpu > 80 || ram > 80) {
                System.out.println("CPU o RAM > 80%. Reducción de lote a 1000...");
                batchSize = 1000; // Reducir a 1000 registros
            } else {
                batchSize = Math.min(incremento, total - start); // 4.2 Incremento de 2000 o el total restante
            }

            // 4.4 Si la latencia sube a 900 ms usar un sleep de 3 seg
            if (latencia > 900) {
                System.out.println("Alta latencia (" + latencia + "ms). Durmiendo 3s...");
                Thread.sleep(3_000);
            }

            // 4.5 Si la latencia baja de 900 ms volver a 1000 registros
            if (latencia <= 900) {
                batchSize = Math.min(incremento, total - start); // Reajustar a los incrementos
            }

            // Procesar el sublote
            int end = Math.min(start + batchSize, total);
            List<?> subLote = registros.subList(start, end);
            System.out.printf("Procesando registros del %d al %d%n", start, end);

            // Simulamos el procesamiento de registros (realiza las operaciones correspondientes aquí)
            // Hacer el procesamiento real de los registros del subLote aquí
            Thread.sleep(500); // Simulación de procesamiento

            start = end; // Incrementamos el índice de inicio del siguiente lote
        }

        System.out.println("Procesamiento batch completado.");
    }

}
