package com.transfer.evaluation;

import com.transfer.evaluation.gui.MainWindow;

import javax.swing.*;

import static com.transfer.evaluation.service.CargadorArchivo.cargarArchivo;


public class Main {
    public static void main(String[] args) {
        try {
            // Usa el look and feel nativo del sistema operativo
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("No se pudo aplicar el Look & Feel nativo.");
        }

        MainWindow ventana = new MainWindow();
        ventana.mostrar();

        /* Prueba de la carga a sql
        String rutaArchivo = "archivos_procesados/procesados_csv_1744231556528.csv";
        cargarArchivo(rutaArchivo);
         */
    }
}

