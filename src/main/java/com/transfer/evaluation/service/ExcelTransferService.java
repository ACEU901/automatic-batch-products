package com.transfer.evaluation.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static com.transfer.evaluation.funcion.Performance.validPerformanceByFile;

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

            Integer multiplo = 1000;
            for (int i = 1; i <= total; i++) {
                if (i % multiplo == 0) {
                    multiplo = validPerformanceByFile(multiplo);
                }
                Row fila = hoja.getRow(i);
                totalLeidos++;
                if (esRegistroCompleto(fila)) {
                    registrosCompletos.add(fila);
                }
                barraProgreso.setValue(i);
            }

            int cantidad = registrosCompletos.size();
            Path destino = escribirNuevoExcel(registrosCompletos.subList(0, cantidad), hoja.getRow(0));

            return new TransferResult(totalLeidos, cantidad, destino);
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

    private static Path escribirNuevoExcel(List<Row> registros, Row encabezado) throws IOException {
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
        return destino;
    }

    // ---------------- CSV ----------------

    private static TransferResult procesarCSV(File archivo, JProgressBar barraProgreso) throws IOException {
        List<String[]> registrosCompletos = new ArrayList<>();
        String[] encabezado = null;
        int totalLeidos = 0;

        List<String> lineas = Files.readAllLines(archivo.toPath());
        barraProgreso.setMaximum(lineas.size());
        barraProgreso.setValue(0);
        Integer multiplo = 1000;
        for (int i = 0; i < lineas.size(); i++) {
            try {
                if (i % multiplo == 0) {
                   multiplo = validPerformanceByFile(multiplo);
                }
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
        Path destino = escribirNuevoCSV(registrosCompletos.subList(0, cantidad), encabezado);

        return new TransferResult(totalLeidos, cantidad, destino);
    }

    private static boolean esRegistroCompleto(String[] columnas) {
        for (String dato : columnas) {
            if (dato == null || dato.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static Path escribirNuevoCSV(List<String[]> registros, String[] encabezado) throws IOException {
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
        return destino;
    }

}
