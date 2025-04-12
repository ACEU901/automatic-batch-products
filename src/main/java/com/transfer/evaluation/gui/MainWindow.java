package com.transfer.evaluation.gui;

import com.transfer.evaluation.service.CargadorArchivo;
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
    private JProgressBar barraProgresoDb;

    public MainWindow() {
        setTitle("Transferencia de Datos desde Excel");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(700, 500));
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 245, 245)); // Fondo claro

        // === PANEL SUPERIOR ===
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(mainPanel.getBackground());

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        botonesPanel.setOpaque(false);

        // Botones con colores personalizados
        JButton cargarBtn = crearBoton("📂 Cargar archivo", new Color(33, 150, 243));      // Azul
        JButton transferirBtn = crearBoton("➡️ Iniciar transferencia", new Color(76, 175, 80)); // Verde
        JButton eliminarBtn = crearBoton("🗑️ Eliminar archivos", new Color(244, 67, 54));      // Rojo

        botonesPanel.add(cargarBtn);
        botonesPanel.add(transferirBtn);
        botonesPanel.add(eliminarBtn);

        archivoLabel = new JLabel("Ningún archivo seleccionado");
        archivoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        archivoLabel.setForeground(Color.DARK_GRAY);
        archivoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));

        topPanel.add(botonesPanel);
        topPanel.add(archivoLabel);

        cargarBtn.addActionListener(e -> seleccionarArchivo());
        transferirBtn.addActionListener(e -> iniciarTransferencia());
        eliminarBtn.addActionListener(e -> eliminarArchivosProcesados());

        // === PANEL INFERIOR ===
        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);
        barraProgreso.setForeground(new Color(100, 149, 237));
        barraProgreso.setPreferredSize(new Dimension(500, 20));

        barraProgresoDb = new JProgressBar();
        barraProgresoDb.setStringPainted(true);
        barraProgresoDb.setForeground(new Color(100, 149, 237));
        barraProgresoDb.setPreferredSize(new Dimension(500, 20));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bottomPanel.setOpaque(false);

        JPanel resumenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        resumenPanel.setOpaque(false);

        labelLeidos = new JLabel("📥 Registros leídos: 0");
        labelLeidos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelCreados = new JLabel("📤 Registros creados: 0");
        labelCreados.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel labelProgresoArchivo = new JLabel("📊 Procesando archivo...");
        JLabel labelProgresoDB = new JLabel("💾 Cargando a la base de datos...");

        resumenPanel.add(labelLeidos);
        resumenPanel.add(labelCreados);

        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(labelProgresoArchivo);
        bottomPanel.add(barraProgreso);
        bottomPanel.add(labelProgresoDB);
        bottomPanel.add(barraProgresoDb);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(resumenPanel);

        // === PANEL CENTRAL ===
        SystemInfoGUI infoGUI = new SystemInfoGUI();
        JPanel panelSistema = infoGUI.crearPanelSistema();
        panelSistema.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Información del Sistema"));
        panelSistema.setBackground(Color.WHITE);

        // === ENSAMBLADO FINAL ===
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(panelSistema, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JButton crearBoton(String texto, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14)); // Asegura soporte para emojis
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBackground(colorFondo);
        boton.setContentAreaFilled(false);
        boton.setOpaque(true);

        // Efectos visuales (hover y clic)
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo);
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo.darker());
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo);
            }
        });

        return boton;
    }

    private void seleccionarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona un archivo Excel o CSV");

        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivos Excel y CSV", "xlsx", "xls", "csv");
        fileChooser.setFileFilter(filtro);

        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = fileChooser.getSelectedFile();
            archivoLabel.setText("📄 Archivo seleccionado: " + archivoSeleccionado.getName());
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
                CargadorArchivo.cargarArchivo(copia, barraProgresoDb);


                SwingUtilities.invokeLater(() -> {
                    labelLeidos.setText("📥 Registros leídos: " + resultado.getRegistrosLeidos());
                    labelCreados.setText("📤 Registros creados: " + resultado.getRegistrosCreados());
                    JOptionPane.showMessageDialog(this, "Transferencia finalizada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (IOException e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Error al procesar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                );
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
