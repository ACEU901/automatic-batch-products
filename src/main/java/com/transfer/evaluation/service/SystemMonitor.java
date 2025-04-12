package com.transfer.evaluation.service;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;

public class SystemMonitor {

    private static final SystemInfo systemInfo = new SystemInfo();
    private static final HardwareAbstractionLayer hal = systemInfo.getHardware();
    private static final OperatingSystem os = systemInfo.getOperatingSystem();

    public static double getCpuUsagePercent() {
        var processor = hal.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long total = 0, idle = 0;

        for (int i = 0; i < ticks.length; i++) {
            total += (ticks[i] - prevTicks[i]);
        }
        idle = ticks[oshi.hardware.CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[oshi.hardware.CentralProcessor.TickType.IDLE.getIndex()];
        return 100.0 * (total - idle) / total;
    }

    public static double getMemoryUsagePercent() {
        GlobalMemory memory = hal.getMemory();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        return 100.0 * (total - available) / total;
    }

    public static long getInternetLatencyMs() {
        try {
            Instant start = Instant.now();
            boolean reachable = InetAddress.getByName("8.8.8.8").isReachable(3000);
            Instant end = Instant.now();
            return reachable ? Duration.between(start, end).toMillis() : 3000;
        } catch (Exception e) {
            return 3000;
        }
    }
}