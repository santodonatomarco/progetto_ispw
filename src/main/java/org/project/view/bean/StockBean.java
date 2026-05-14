package org.project.view.bean;

/**
 * Bean per trasportare i dati di uno Stock tra controller e view.
 * Non contiene logica né riferimenti al model — solo dati piatti.
 */
public class StockBean {

    private String simbolo;
    private String nomeAzienda;
    private String settore;
    private double prezzoAttuale;
    private double variazioneGiornaliera;   // es. +1.5 = +1.5%
    private double variazioneSettimanale;
    private double marketCap;
    private double volumeSettimanale;

    public StockBean(String simbolo, String nomeAzienda, String settore, double prezzoAttuale) {
        this.simbolo = simbolo;
        this.nomeAzienda = nomeAzienda;
        this.settore = settore;
        this.prezzoAttuale = prezzoAttuale;
    }

    public String getSimbolo()                  { return simbolo; }
    public String getNomeAzienda()              { return nomeAzienda; }
    public String getSettore()                  { return settore; }
    public double getPrezzoAttuale()            { return prezzoAttuale; }
    public double getVariazioneGiornaliera()    { return variazioneGiornaliera; }
    public double getVariazioneSettimanale()    { return variazioneSettimanale; }
    public double getMarketCap()                { return marketCap; }
    public double getVolumeSettimanale()        { return volumeSettimanale; }

    public void setSimbolo(String simbolo)                          { this.simbolo = simbolo; }
    public void setNomeAzienda(String nomeAzienda)                  { this.nomeAzienda = nomeAzienda; }
    public void setSettore(String settore)                          { this.settore = settore; }
    public void setPrezzoAttuale(double prezzoAttuale)              { this.prezzoAttuale = prezzoAttuale; }
    public void setVariazioneGiornaliera(double v)                  { this.variazioneGiornaliera = v; }
    public void setVariazioneSettimanale(double v)                  { this.variazioneSettimanale = v; }
    public void setMarketCap(double marketCap)                      { this.marketCap = marketCap; }
    public void setVolumeSettimanale(double volumeSettimanale)      { this.volumeSettimanale = volumeSettimanale; }
}