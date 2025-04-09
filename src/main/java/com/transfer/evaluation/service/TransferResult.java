package com.transfer.evaluation.service;

public class TransferResult {
    private int registrosLeidos;
    private int registrosCreados;

    public TransferResult(int leidos, int creados) {
        this.registrosLeidos = leidos;
        this.registrosCreados = creados;
    }

    public int getRegistrosLeidos() {
        return registrosLeidos;
    }

    public int getRegistrosCreados() {
        return registrosCreados;
    }
}
