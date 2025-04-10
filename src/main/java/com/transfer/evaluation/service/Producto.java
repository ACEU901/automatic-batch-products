package com.transfer.evaluation.service;

public class Producto {
    private Float numSuc;
    private String sku;
    private String codBarra;
    private String descripcion;
    private Float fam;
    private Float salFisSuc;
    private Float valor;

    // Constructor vacío
    public Producto() {}

    // Constructor con todos los campos
    public Producto(Float numSuc, String sku, String codBarra, String descripcion,
                    Float fam, Float salFisSuc, Float valor) {
        this.numSuc = numSuc;
        this.sku = sku;
        this.codBarra = codBarra;
        this.descripcion = descripcion;
        this.fam = fam;
        this.salFisSuc = salFisSuc;
        this.valor = valor;
    }

    // Getters y Setters

    public Float getNumSuc() {
        return numSuc;
    }

    public void setNumSuc(Float numSuc) {
        this.numSuc = numSuc;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCodBarra() {
        return codBarra;
    }

    public void setCodBarra(String codBarra) {
        this.codBarra = codBarra;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Float getFam() {
        return fam;
    }

    public void setFam(Float fam) {
        this.fam = fam;
    }

    public Float getSalFisSuc() {
        return salFisSuc;
    }

    public void setSalFisSuc(Float salFisSuc) {
        this.salFisSuc = salFisSuc;
    }

    public Float getValor() {
        return valor;
    }

    public void setValor(Float valor) {
        this.valor = valor;
    }
}
