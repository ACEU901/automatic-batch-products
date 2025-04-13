package com.transfer.evaluation.funcion;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.List;

public class DbValidation {

    public static boolean readLines(List<String[]> lineas, boolean esExcel, File archivo){
        try {
            if (esExcel) {
                try (FileInputStream fis = new FileInputStream(archivo);
                     Workbook workbook = new XSSFWorkbook(fis)) {
                    Sheet sheet = workbook.getSheetAt(0);
                    boolean esPrimeraFila = true;
                    for (Row row : sheet) {
                        if (esPrimeraFila) {
                            esPrimeraFila = false;
                            continue;
                        }
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
                    boolean esPrimeraLinea = true;
                    while ((linea = br.readLine()) != null) {
                        if (esPrimeraLinea) {
                            esPrimeraLinea = false;
                            continue;
                        }
                        lineas.add(linea.split(";"));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            return false;
        }
        return true;
    }


}
