package org.project.model;

import org.project.ing.enumerations.StatoTransazione;
import org.project.ing.enumerations.TipoTransazione;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Transaction {

    private Stock stock;
    private TipoTransazione tipo;        // BUY o SELL
    private StatoTransazione stato;     // DONE o PENDING
    private double quantita;
    private double prezzoAlMomento;      // prezzo dello stock al momento della transazione
    private double importoTotale;        // quantita * prezzoAlMomento
    private LocalDateTime timestamp;
    private String emailStudente;

    public Transaction(Stock stock, TipoTransazione tipo, double quantita, double prezzoAlMomento) {
        this.collegaStock(stock);
        this.stabilisciTipo(tipo);
        this.impostaQuantita(quantita);
        this.registraPrezzo(prezzoAlMomento);
        this.stato = StatoTransazione.PENDING;  // inizia sempre come pending
        this.timestamp = LocalDateTime.now(ZoneId.systemDefault());
    }

    // Costruttore usato per ricreare una transazione esistente (quando si legge dal DB/file)
    public Transaction(Stock stock, TipoTransazione tipo, double quantita, double prezzoAlMomento, LocalDateTime timestampOriginale) {
        this.collegaStock(stock);
        this.stabilisciTipo(tipo);
        this.impostaQuantita(quantita);
        this.registraPrezzo(prezzoAlMomento);
        this.stato = StatoTransazione.PENDING;
        if (timestampOriginale == null) {
            this.timestamp = LocalDateTime.now(ZoneId.systemDefault());
        } else {
            this.timestamp = timestampOriginale;
        }
    }

    public final void collegaStock(Stock stock) {
        if (stock == null)
            throw new IllegalArgumentException("Lo stock non può essere nullo.");
        this.stock = stock;
    }

    public final void stabilisciTipo(TipoTransazione tipo) {
        if (tipo == null)
            throw new IllegalArgumentException("Il tipo transazione è obbligatorio.");
        this.tipo = tipo;
    }

    public final void impostaQuantita(double quantita) {
        if (quantita <= 0)
            throw new IllegalArgumentException("La quantità deve essere positiva.");
        this.quantita = quantita;
        this.ricalcolaImporto();
    }

    public final void registraPrezzo(double prezzo) {
        if (prezzo < 0)
            throw new IllegalArgumentException("Il prezzo non può essere negativo.");
        this.prezzoAlMomento = prezzo;
        this.ricalcolaImporto();
    }

    public final void completaTransazione() {
        this.stato = StatoTransazione.DONE;
    }

    // Permette di aggiornare il timestamp (utile quando si aggregano transazioni)
    public final void aggiornaTimestamp(java.time.LocalDateTime nuovoTimestamp) {
        if (nuovoTimestamp == null) throw new IllegalArgumentException("Timestamp non può essere nullo.");
        this.timestamp = nuovoTimestamp;
    }

    public final void impostaEmailStudente(String email) {
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Email studente non valida.");
        this.emailStudente = email;
    }

    private void ricalcolaImporto() {
        this.importoTotale = this.quantita * this.prezzoAlMomento;
    }

    public Stock stock()                { return stock; }
    public TipoTransazione tipo()       { return tipo; }
    public StatoTransazione stato()    { return stato; }
    public double quantita()            { return quantita; }
    public double prezzoAlMomento()     { return prezzoAlMomento; }
    public double importoTotale()       { return importoTotale; }
    public LocalDateTime quando()       { return timestamp; }
    public String emailStudente()    { return emailStudente; }
}