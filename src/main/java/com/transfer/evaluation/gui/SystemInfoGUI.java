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

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Información del Sistema");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(infoArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Hilo para actualizar la información en tiempo real
        new Thread(() -> {
            while (true) {
                updateSystemInfo(infoArea);
                try {
                    Thread.sleep(1000); // Actualiza cada segundo
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        frame.setVisible(true);
    }

    private static void updateSystemInfo(JTextArea info) {
        SystemInfo si = new SystemInfo();

        // Sistema Operativo
        OperatingSystem os = si.getOperatingSystem();
        String osName = os.toString();

        // Procesador
        CentralProcessor cpu = si.getHardware().getProcessor();
        String processorName = cpu.getProcessorIdentifier().getName();
        int physicalCores = cpu.getPhysicalProcessorCount();
        int logicalCores = cpu.getLogicalProcessorCount();
        double cpuLoad = cpu.getSystemCpuLoad(1000) * 100; // Se pasa un parámetro en ms

        // Memoria RAM
        GlobalMemory memory = si.getHardware().getMemory();
        long totalMem = memory.getTotal();
        long usedMem = totalMem - memory.getAvailable();

        // Disco
        HWDiskStore disk = si.getHardware().getDiskStores().get(0);
        long totalDisk = disk.getSize();
        long usedDisk = disk.getReads() + disk.getWrites(); // Alternativa a getFreeSpace()

        // Interfaces de red activas
        List<NetworkIF> networkIFs = si.getHardware().getNetworkIFs();
        double totalUpload = 0;
        double totalDownload = 0;
        for (NetworkIF net : networkIFs) {
            if (net.getIfOperStatus().equals(NetworkIF.IfOperStatus.UP)) { // Solo interfaces activas
                totalUpload += net.getBytesSent();
                totalDownload += net.getBytesRecv();
            }
        }

        // Mostrar la información en la interfaz gráfica
        info.setText(
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
                        String.format("Bajada: %.2f MB\n", totalDownload / (1024 * 1024))
        );
    }
}
