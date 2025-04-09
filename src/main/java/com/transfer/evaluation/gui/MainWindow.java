package com.transfer.evaluation.gui;

import com.transfer.evaluation.service.ExcelTransferService;
import com.transfer.evaluation.service.TransferResult;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MainWindow extends JFrame {

    private JLabel archivoLabel;
    private JLabel labelLeidos;
    private JLabel labelCreados;
    private File archivoSeleccionado;
    private JProgressBar barraProgreso;

    public MainWindow() {
        setTitle("Transferencia desde Excel a Base de Datos");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Panel superior
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton cargarBtn = new JButton("Cargar archivo Excel/CSV");
        JButton transferirBtn = new JButton("Iniciar transferencia");
        JButton eliminarBtn = new JButton("Eliminar archivos generados");

        archivoLabel = new JLabel("Ningún archivo seleccionado");
        labelLeidos = new JLabel("Registros leídos: 0");
        labelCreados = new JLabel("Registros creados: 0");

        cargarBtn.addActionListener(e -> seleccionarArchivo());
        transferirBtn.addActionListener(e -> iniciarTransferencia());
        eliminarBtn.addActionListener(e -> eliminarArchivosProcesados());

        panelTop.add(cargarBtn);
        panelTop.add(transferirBtn);
        panelTop.add(eliminarBtn);
        panelTop.add(archivoLabel);

        // Panel inferior
        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);

        JPanel panelBottom = new JPanel(new GridLayout(2, 1));
        panelBottom.add(barraProgreso);

        JPanel resumenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resumenPanel.add(labelLeidos);
        resumenPanel.add(labelCreados);
        panelBottom.add(resumenPanel);

        // Panel central
        SystemInfoGUI infoGUI = new SystemInfoGUI();
        JPanel panelSistema = infoGUI.crearPanelSistema();

        add(panelTop, BorderLayout.NORTH);
        add(panelSistema, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private void seleccionarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona un archivo Excel o CSV");

        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivos Excel y CSV", "xlsx", "xls", "csv");
        fileChooser.setFileFilter(filtro);

        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = fileChooser.getSelectedFile();
            archivoLabel.setText("Archivo: " + archivoSeleccionado.getName());
        }
    }

    private void iniciarTransferencia() {
        if (archivoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero selecciona un archivo Excel o CSV.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                File copia = ExcelTransferService.copiarArchivo(archivoSeleccionado);
                TransferResult resultado = ExcelTransferService.procesarArchivo(copia, barraProgreso);

                SwingUtilities.invokeLater(() -> {
                    labelLeidos.setText("Registros leídos: " + resultado.getRegistrosLeidos());
                    labelCreados.setText("Registros creados: " + resultado.getRegistrosCreados());
                    JOptionPane.showMessageDialog(this, "Transferencia finalizada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (IOException e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Error al procesar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                );
                e.printStackTrace();
            }
        }).start();
    }

    private void eliminarArchivosProcesados() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Deseas eliminar todos los archivos generados (cargados y procesados)?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        eliminarArchivosEnCarpeta("archivos_procesados");
        eliminarArchivosEnCarpeta("archivos_cargados");

        JOptionPane.showMessageDialog(this, "Archivos eliminados correctamente.");
    }

    private void eliminarArchivosEnCarpeta(String nombreCarpeta) {
        File carpeta = new File(nombreCarpeta);
        if (carpeta.exists() && carpeta.isDirectory()) {
            File[] archivos = carpeta.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    archivo.delete();
                }
            }
        }
    }


    public void mostrar() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}