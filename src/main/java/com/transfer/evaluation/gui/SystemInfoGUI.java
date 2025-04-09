package com.transfer.evaluation.gui;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SystemInfoGUI {

    private JTextArea infoArea;

    public JPanel crearPanelSistema() {
        JPanel panel = new JPanel(new BorderLayout());

        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(infoArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Hilo para actualizar la información en tiempo real
        new Thread(() -> {
            while (true) {
                updateSystemInfo();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return panel;
    }

    private void updateSystemInfo() {
        SystemInfo si = new SystemInfo();

        // Sistema Operativo
        OperatingSystem os = si.getOperatingSystem();
        String osName = os.toString();

        // Procesador
        CentralProcessor cpu = si.getHardware().getProcessor();
        String processorName = cpu.getProcessorIdentifier().getName();
        int physicalCores = cpu.getPhysicalProcessorCount();
        int logicalCores = cpu.getLogicalProcessorCount();
        double cpuLoad = cpu.getSystemCpuLoad(1000) * 100;

        // Memoria RAM
        GlobalMemory memory = si.getHardware().getMemory();
        long totalMem = memory.getTotal();
        long usedMem = totalMem - memory.getAvailable();

        // Disco
        List<HWDiskStore> disks = si.getHardware().getDiskStores();
        long totalDisk = 0;
        long usedDisk = 0;
        if (!disks.isEmpty()) {
            HWDiskStore disk = disks.get(0);
            totalDisk = disk.getSize();
            usedDisk = disk.getReads() + disk.getWrites();
        }

        // Red
        List<NetworkIF> networkIFs = si.getHardware().getNetworkIFs();
        double totalUpload = 0;
        double totalDownload = 0;
        for (NetworkIF net : networkIFs) {
            if (net.getIfOperStatus().equals(NetworkIF.IfOperStatus.UP)) {
                totalUpload += net.getBytesSent();
                totalDownload += net.getBytesRecv();
            }
        }

        String infoTexto =
                "Sistema Operativo: " + osName + "\n\n" +
                        "Procesador: " + processorName + "\n" +
                        "Núcleos físicos: " + physicalCores + "\n" +
                        "Núcleos lógicos: " + logicalCores + "\n" +
                        String.format("Uso del CPU: %.2f%%\n\n", cpuLoad) +

                        "Memoria RAM:\n" +
                        (usedMem / (1024 * 1024)) + " / " + (totalMem / (1024 * 1024)) + " MB\n\n" +

                        "Disco:\n" +
                        (usedDisk / (1024 * 1024 * 1024)) + " / " + (totalDisk / (1024 * 1024 * 1024)) + " GB\n\n" +

                        "Red:\n" +
                        String.format("Subida: %.2f MB\n", totalUpload / (1024 * 1024)) +
                        String.format("Bajada: %.2f MB\n", totalDownload / (1024 * 1024));

        infoArea.setText(infoTexto);
    }

    // Método opcional para pruebas independientes
    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Información del Sistema");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);

        SystemInfoGUI sistema = new SystemInfoGUI();
        JPanel panel = sistema.crearPanelSistema();
        frame.add(panel);

        frame.setVisible(true);
    }
}
