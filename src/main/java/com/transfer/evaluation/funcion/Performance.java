package com.transfer.evaluation.funcion;

import com.transfer.evaluation.service.SystemMonitor;

public class Performance {

    public static Integer validPerformance(Integer multiplo) throws InterruptedException {
        double cpu = SystemMonitor.getCpuUsagePercent();
        double ram = SystemMonitor.getMemoryUsagePercent();
        long latencia = SystemMonitor.getInternetLatencyMs();
        Integer multiploBase = 1000;

        if (cpu > 90 || ram > 90) {
            System.out.println("CPU o RAM > 90%.");
            multiplo = multiploBase;
            Thread.sleep(10_000);
        } else if (cpu > 80 || ram > 80) {
            System.out.println("CPU o RAM > 80%. Durmiendo 1s...");
            multiplo = multiploBase;
            Thread.sleep(10_000);

        } else if (latencia > 900) {
            System.out.println("Alta latencia (" + latencia + "ms). Durmiendo 3s...");
            multiplo = multiploBase;
            Thread.sleep(3_000);
        }else if (latencia < 100 && cpu < 30 && ram <60) {
            System.out.println("Bajo consumo de recursos:  ("+"cpu "+cpu+" %" +"ram "+ram+" %" + latencia + "ms)");
            multiplo += 10000;
        } else {
            multiplo +=4000;
        }
        return multiplo;
    }

    public static Integer validPerformanceByDb(Integer multiplo) throws InterruptedException {
        double cpu = SystemMonitor.getCpuUsagePercent();
        double ram = SystemMonitor.getMemoryUsagePercent();
        long latencia = SystemMonitor.getInternetLatencyMs();
        Integer multiploBase = 10000;

        if (cpu > 90 || ram > 90) {
            System.out.println("CPU o RAM > 90%.");
            multiplo = multiploBase;
            Thread.sleep(4_000);
        } else if (cpu > 80 || ram > 80) {
            System.out.println("CPU o RAM > 80%. Durmiendo 1s...");
            multiplo = multiploBase;
            Thread.sleep(5_000);

        } else if (latencia > 900) {
            System.out.println("Alta latencia (" + latencia + "ms). Durmiendo 3s...");
            multiplo = multiploBase;
            Thread.sleep(6_000);
        }else if (latencia < 100 && cpu < 30 && ram <60) {
            System.out.println("Bajo consumo de recursos:  ("+"cpu "+cpu+" %" +"ram "+ram+" %" + latencia + "ms)");
            multiplo += 15000;
        } else {
            multiplo +=10000;
        }
        return multiplo;
    }
}
