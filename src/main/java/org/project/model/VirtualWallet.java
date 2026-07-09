package org.project.model;

import org.project.ing.enumerations.TipoTransazione;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VirtualWallet {

    private Studente owner;
    private double saldoDisponibile;
    private List<WalletPosition> posizioni;
    private List<Transaction> transazioni;

    public VirtualWallet(Studente owner, double saldoIniziale) {
        this.collegaStudente(owner);
        this.impostaSaldo(saldoIniziale);
        this.posizioni = new ArrayList<>();
        this.transazioni = new ArrayList<>();
    }

    public final void collegaStudente(Studente owner) {
        this.owner = owner;
    }

    public final void impostaSaldo(double saldo) {
        if (saldo < 0)
            throw new IllegalArgumentException("Il saldo non può essere negativo.");
        this.saldoDisponibile = saldo;
    }

    public final void aggiungiTransazione(Transaction t) {
        if (t == null)
            throw new IllegalArgumentException("La transazione non può essere nulla.");
        this.transazioni.add(t);
    }

    public final void aggiungiPosizione(WalletPosition p) {
        if (p == null)
            throw new IllegalArgumentException("La posizione non può essere nulla.");
        this.posizioni.add(p);
    }

    public final void scalaSaldo(double importo) {
        if (importo > saldoDisponibile)
            throw new IllegalArgumentException("Saldo insufficiente.");
        this.saldoDisponibile -= importo;
    }

    public final void accreditaSaldo(double importo) {
        if (importo < 0)
            throw new IllegalArgumentException("L'importo da accreditare non può essere negativo.");
        this.saldoDisponibile += importo;
    }


    public final WalletPosition eseguiAcquisto(Stock stock, double quantita, double prezzo) {
        double importo = quantita * prezzo;
        scalaSaldo(importo);

        WalletPosition posizione = trovaPosizione(stock);
        if (posizione == null) {
            posizione = new WalletPosition(stock, quantita, prezzo);
            stock.aggiungiObserver(posizione);   // unica registrazione, qui
            aggiungiPosizione(posizione);
        } else {
            posizione.aggiungiAzioni(quantita, prezzo);
        }
        return posizione;
    }

    // Deve essere chiamato prima che questo wallet venga abbandonato.
    // Deregistra tutte le WalletPosition dagli Stock, rompendo il ciclo
    // che altrimenti impedirebbe al GC di raccogliere le part.

    public void chiudi() {
        for (WalletPosition p : posizioni) {
            p.stock().rimuoviObserver(p);
        }
        posizioni.clear();
        transazioni.clear();
    }

    // Valore totale = saldo liquido + valore di tutte le posizioni
    public double calcolaTotalePortafoglio() {
        double totale = saldoDisponibile;
        for (WalletPosition p : posizioni) {
            totale += p.valoreAttuale();
        }
        return totale;
    }

    // Cerca una posizione esistente per quello stock
    public WalletPosition trovaPosizione(Stock stock) {
        for (WalletPosition p : posizioni) {
            if (p.stock().simbolo().equals(stock.simbolo())) {
                return p;
            }
        }
        return null;
    }

    // Usato solo dai DAO per ricostruire una posizione salvata —
    // non tocca il saldo, non è un acquisto nuovo.

    public WalletPosition caricaPosizione(Stock stock, double quantita, double prezzoMedio) {
        WalletPosition p = new WalletPosition(stock, quantita, prezzoMedio);
        stock.aggiungiObserver(p);
        aggiungiPosizione(p);
        return p;
    }

    // Usato solo dai DAO per ricostruire una transazione salvata.
    public Transaction caricaTransazione(Stock stock, TipoTransazione tipo,
                                         double quantita, double prezzo,
                                         LocalDateTime ts, boolean completata) {
        Transaction t = new Transaction(stock, tipo, quantita, prezzo, ts);
        if (completata) t.completaTransazione();
        aggiungiTransazione(t);
        return t;
    }




    public Studente proprietario()           { return owner; }
    public double saldoDisponibile()         { return saldoDisponibile; }
    public List<WalletPosition> posizioni()  { return posizioni; }
    public List<Transaction> transazioni()   { return transazioni; }
}