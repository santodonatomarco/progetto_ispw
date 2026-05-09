package org.project.model;

import org.project.ing.observer.StockSubject;

import java.time.LocalDateTime;

public class Stock extends StockSubject {

    private String simbolo;
    private String nomeAzienda;
    private String settore;
    private double prezzoAttuale;
    private double variazioneGiornaliera;
    private double variazioneSettimanale;
    private double marketCap;
    private double volumeSettimanale;
    private LocalDateTime ultimoAggiornamento;


    public Stock(String simbolo, String nomeAzienda, String settore, double prezzoAttuale) {
        this.impostaSimbolo(simbolo);
        this.battezzaAzienda(nomeAzienda);
        this.classificaSector(settore);
        this.aggiornaPrezzo(prezzoAttuale);
    }

    public final void impostaSimbolo(String simbolo) {
        if (simbolo == null || simbolo.trim().isEmpty())
            throw new IllegalArgumentException("Il simbolo non può essere vuoto.");
        this.simbolo = simbolo.toUpperCase();
    }

    public final void battezzaAzienda(String nome) {
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Il nome azienda non può essere vuoto.");
        this.nomeAzienda = nome;
    }

    public final void classificaSector(String sector) {
        if (sector == null || sector.trim().isEmpty())
            throw new IllegalArgumentException("Il settore non può essere vuoto.");
        this.settore = sector;
    }

    // Quando si aggiorna il prezzo — notifica automaticamente
    public final void aggiornaPrezzo(double nuovoPrezzo) {
        if (nuovoPrezzo < 0)
            throw new IllegalArgumentException("Prezzo non può essere negativo.");
        this.prezzoAttuale = nuovoPrezzo;
        this.ultimoAggiornamento = LocalDateTime.now();
        this.notificaObserver();  // <-- notifica automatica!
    }


    public final void aggiornaVariazioni(double daily, double weekly) {
        this.variazioneGiornaliera = daily;
        this.variazioneSettimanale = weekly;
    }

    public final void aggiornaMarketData(double marketCap, double volume) {
        this.marketCap = marketCap;
        this.volumeSettimanale = volume;
    }

    public String simbolo()             { return simbolo; }
    public String nomeAzienda()         { return nomeAzienda; }
    public String settore()             { return settore; }
    public double prezzoAttuale()       { return prezzoAttuale; }
    public double variazioneGiornaliera() { return variazioneGiornaliera; }
    public double variazioneSettimanale() { return variazioneSettimanale; }
    public double marketCap()           { return marketCap; }
    public double volumeSettimanale()   { return volumeSettimanale; }
    public LocalDateTime ultimoAggiornamento() { return ultimoAggiornamento; }



}