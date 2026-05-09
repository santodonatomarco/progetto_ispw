package org.project.model;

import org.project.ing.observer.StockObserver;

public class WalletPosition implements StockObserver {
    private Stock stock;
    private double quantita;
    private double prezzoMedioAcquisto;   // media ponderata dei prezzi pagati
    private double valoreCorrente;

    public WalletPosition(Stock stock, double quantita, double prezzoAcquisto) {
        this.collegaStock(stock);
        this.impostaQuantita(quantita);
        this.impostaPrezzoMedio(prezzoAcquisto);
    }

    public final void collegaStock(Stock stock) {
        if (stock == null)
            throw new IllegalArgumentException("Lo stock non può essere nullo.");
        this.stock = stock;
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

    // Chiamato quando si acquistano altre azioni dello stesso stock
    public final void aggiungiAzioni(double nuovaQuantita, double prezzoNuovo) {
        double totaleInvestito = (this.quantita * this.prezzoMedioAcquisto)
                + (nuovaQuantita * prezzoNuovo);
        this.quantita += nuovaQuantita;
        this.prezzoMedioAcquisto = totaleInvestito / this.quantita;
    }

    // Chiamato quando si vendono azioni
    public final void rimuoviAzioni(double quantitaVenduta) {
        if (quantitaVenduta > this.quantita)
            throw new IllegalArgumentException("Non puoi vendere più azioni di quante ne possiedi.");
        this.quantita -= quantitaVenduta;
    }

    // Profitto/perdita attuale su questa posizione
    public double calcolaProfittoPerdita() {
        return (stock.prezzoAttuale() - prezzoMedioAcquisto) * quantita;
    }

    // Valore attuale della posizione
    public double valoreAttuale() {
        return stock.prezzoAttuale() * quantita;
    }

    @Override
    public void aggiornamento() {
        // quando lo stock cambia prezzo, ricalcola il valore
        this.valoreCorrente = stock.prezzoAttuale() * this.quantita;
    }


    public Stock stock()                  { return stock; }
    public double quantita()              { return quantita; }
    public double prezzoMedioAcquisto()   { return prezzoMedioAcquisto; }
    public double valoreCorrente()        { return valoreCorrente; }
}
