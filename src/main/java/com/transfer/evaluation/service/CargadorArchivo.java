package com.transfer.evaluation.service;

import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

import static com.transfer.evaluation.funcion.DbValidation.lecturaLineas;
import static com.transfer.evaluation.funcion.DbValidation.validInsert;

public class CargadorArchivo {

    public static void cargarArchivo(File archivo, JProgressBar barraProgresoDb) {
        List<String[]> lineas = new ArrayList<>();
        boolean esExcel = archivo.getName().endsWith(".xlsx") || archivo.getName().endsWith(".xls");
        boolean continuar = true;
        // 1. Leer todas las líneas del archivo según el tipo
        continuar = lecturaLineas(lineas, esExcel, archivo);
        if (!continuar) {
            return;
        }
        // 2. Conexión a la base de datos
        validInsert(lineas, barraProgresoDb);
    }

}
