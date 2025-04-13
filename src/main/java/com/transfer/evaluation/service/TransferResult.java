package com.transfer.evaluation.service;

import java.nio.file.Path;

public class TransferResult {
    private int registrosLeidos;
    private int registrosCreados;
    private Path destino;

    public TransferResult(int leidos, int creados, Path destino) {
        this.registrosLeidos = leidos;
        this.registrosCreados = creados;
        this.destino = destino;
    }

    public int getRegistrosLeidos() {
        return registrosLeidos;
    }

    public int getRegistrosCreados() {
        return registrosCreados;
    }

    public Path getDestino() {
        return destino;
    }
}
