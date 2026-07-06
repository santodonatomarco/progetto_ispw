package org.project.view.bean;

public class AvvioOrdineBean {

    private final String simbolo;

    public AvvioOrdineBean(String simbolo) {
        if (simbolo == null || simbolo.isBlank())
            throw new IllegalArgumentException("Il simbolo non può essere vuoto.");
        this.simbolo = simbolo.trim().toUpperCase();
    }

    public String getSimbolo() {
        return simbolo;
    }
}