package org.project.view.bean;


public class RicercaStockBean {

    private final String simbolo;

    public RicercaStockBean(String simbolo) {
        if (simbolo == null || simbolo.isBlank())
            throw new IllegalArgumentException("Il simbolo non può essere vuoto.");
        this.simbolo = simbolo.trim().toUpperCase();
    }

    public String getSimbolo() { return simbolo; }
}