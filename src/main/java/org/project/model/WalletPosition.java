package org.project.model;

import org.project.ing.observer.StockObserver;

public class WalletPosition implements StockObserver {

    private Stock stock;
    private double quantita;
    private double prezzoMedioAcquisto;
    private double valoreCorrente;

    public WalletPosition(Stock stock, double quantita, double prezzoAcquisto) {
        this.collegaStock(stock);
        this.impostaQuantita(quantita);
        this.impostaPrezzoMedio(prezzoAcquisto);
        this.valoreCorrente = stock.prezzoAttuale() * quantita;
    }

    public final void collegaStock(Stock stock) {
        if (stock == null)
            throw new IllegalArgumentException("Lo stock non può essere nullo.");
        this.stock = stock;
        stock.aggiungiObserver(this);  // si registra come observer al momento del collegamento
    }

    public final void impostaQuantita(double quantita) {
        if (quantita < 0)
            throw new IllegalArgumentException("La quantità non può essere negativa.");
        this.quantita = quantita;
    }

    public final void impostaPrezzoMedio(double prezzo) {
        if (prezzo < 0)
            throw new IllegalArgumentException("Il prezzo medio non può essere negativo.");
        this.prezzoMedioAcquisto = prezzo;
    }

    public final void aggiungiAzioni(double nuovaQuantita, double prezzoNuovo) {
        double totaleInvestito = (this.quantita * this.prezzoMedioAcquisto)
                + (nuovaQuantita * prezzoNuovo);
        this.quantita += nuovaQuantita;
        this.prezzoMedioAcquisto = totaleInvestito / this.quantita;
        this.valoreCorrente = this.stock.prezzoAttuale() * this.quantita;
    }

    public final void rimuoviAzioni(double quantitaVenduta) {
        if (quantitaVenduta > this.quantita)
            throw new IllegalArgumentException("Non puoi vendere più azioni di quante ne possiedi.");
        this.quantita -= quantitaVenduta;
        this.valoreCorrente = this.stock.prezzoAttuale() * this.quantita;
    }

    public double calcolaProfittoPerdita() {
        return (stock.prezzoAttuale() - prezzoMedioAcquisto) * quantita;
    }

    public double valoreAttuale() {
        return stock.prezzoAttuale() * quantita;
    }

    /** Chiamato automaticamente da Stock.aggiornaPrezzo() tramite observer. */
    @Override
    public void aggiornamento(Stock stock) {
        this.valoreCorrente = stock.prezzoAttuale() * this.quantita;
    }

    public Stock stock()                { return stock; }
    public double quantita()            { return quantita; }
    public double prezzoMedioAcquisto() { return prezzoMedioAcquisto; }
    public double valoreCorrente()      { return valoreCorrente; }
}
