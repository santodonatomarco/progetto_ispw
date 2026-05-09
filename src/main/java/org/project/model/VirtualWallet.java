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
        if (owner == null)
            throw new IllegalArgumentException("Il proprietario non può essere nullo.");
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
        return null;  // non possiede quello stock
    }

    public Studente proprietario()           { return owner; }
    public double saldoDisponibile()        { return saldoDisponibile; }
    public List<WalletPosition> posizioni() { return posizioni; }
    public List<Transaction> transazioni()  { return transazioni; }
}
