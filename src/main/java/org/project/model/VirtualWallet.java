package org.project.model;

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

    /**
     * Esegue un acquisto: scala il saldo, crea o aggiorna la WalletPosition,
     * registra la posizione come observer dello stock.
     *
     * Incapsula tutta la logica di business dell'acquisto (Expert Pattern GRASP)
     * — il controller non deve sapere come funziona internamente.
     *
     * @param stock    lo stock da acquistare
     * @param quantita numero di azioni
     * @param prezzo   prezzo unitario al momento dell'acquisto
     * @return la WalletPosition aggiornata o creata (nuova o esistente)
     */
    public final WalletPosition eseguiAcquisto(Stock stock, double quantita, double prezzo) {
        double importo = quantita * prezzo;
        scalaSaldo(importo);

        WalletPosition posizione = trovaPosizione(stock);
        if (posizione == null) {
            posizione = new WalletPosition(stock, quantita, prezzo);
            stock.aggiungiObserver(posizione);
            aggiungiPosizione(posizione);
        } else {
            posizione.aggiungiAzioni(quantita, prezzo);
        }

        return posizione;
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

    public Studente proprietario()           { return owner; }
    public double saldoDisponibile()         { return saldoDisponibile; }
    public List<WalletPosition> posizioni()  { return posizioni; }
    public List<Transaction> transazioni()   { return transazioni; }
}