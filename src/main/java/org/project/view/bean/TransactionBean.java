package org.project.view.bean;

import org.project.ing.enumerations.StatoTransazione;
import org.project.ing.enumerations.TipoTransazione;

import java.time.LocalDateTime;


public class TransactionBean {

    private StockBean stock;
    private TipoTransazione tipo;       // BUY o SELL
    private StatoTransazione stato;     // PENDING o DONE
    private double quantita;
    private double prezzoAlMomento;
    private double importoTotale;       // quantita * prezzoAlMomento
    private LocalDateTime quando;

    public TransactionBean() {}

    public TransactionBean(StockBean stock, TipoTransazione tipo, StatoTransazione stato,
                           double quantita, double prezzoAlMomento,
                           double importoTotale, LocalDateTime quando) {
        this.stock = stock;
        this.tipo = tipo;
        this.stato = stato;
        this.quantita = quantita;
        this.prezzoAlMomento = prezzoAlMomento;
        this.importoTotale = importoTotale;
        this.quando = quando;
    }

    public StockBean getStock()             { return stock; }
    public TipoTransazione getTipo()        { return tipo; }
    public StatoTransazione getStato()      { return stato; }
    public double getQuantita()             { return quantita; }
    public double getPrezzoAlMomento()      { return prezzoAlMomento; }
    public double getImportoTotale()        { return importoTotale; }
    public LocalDateTime getQuando()        { return quando; }

    public void setStock(StockBean stock)               { this.stock = stock; }
    public void setTipo(TipoTransazione tipo)           { this.tipo = tipo; }
    public void setStato(StatoTransazione stato)        { this.stato = stato; }
    public void setQuantita(double quantita)            { this.quantita = quantita; }
    public void setPrezzoAlMomento(double prezzo)       { this.prezzoAlMomento = prezzo; }
    public void setImportoTotale(double importo)        { this.importoTotale = importo; }
    public void setQuando(LocalDateTime quando)         { this.quando = quando; }
}