package org.project.model;

import org.project.ing.enumerations.AuthProvider;
import org.project.ing.enumerations.Ruolo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Studente extends Utente {

    private String passwordHash; // usato solo se authProvider == LOCAL, altrimenti ""
    private SchoolClass schoolClass;
    private VirtualWallet wallet;
    private List<Utente> amici;

    public Studente(String email, String nome, String cognome, AuthProvider provider) {
        super(email, nome, cognome, provider);
        this.passwordHash = "";
        this.amici = new ArrayList<>();
    }

    // ── Gestione password (solo LOCAL) ───

    public void impostaPasswordHash(String hash) {
        if (hash == null || hash.trim().isEmpty())
            throw new IllegalArgumentException("La password hash non può essere vuota.");
        this.passwordHash = hash;
    }

    public String getPasswordHash() { return passwordHash; }


    public final void iscriviClasse(SchoolClass classe) {
        if (classe == null)
            throw new IllegalArgumentException("La classe non può essere nulla.");
        this.schoolClass = classe;
    }

    public final void creaWallet(double saldoIniziale) {
        if (saldoIniziale < 0)
            throw new IllegalArgumentException("Il saldo iniziale non può essere negativo.");
        if (this.wallet != null)
            this.wallet.chiudi();          // chiudi il vecchio prima di rimpiazzarlo
        this.wallet = new VirtualWallet(this, saldoIniziale);
    }

    public final void aggiungiAmico(Utente amico) {
        if (amico == null || amico.equals(this))
            throw new IllegalArgumentException("Amico non valido.");
        if (!this.amici.contains(amico))
            this.amici.add(amico);
    }


    public SchoolClass classeFrequentata() { return schoolClass; }
    public VirtualWallet portafoglio()     { return wallet; }
    public List<Utente> presentaAmici()    { return Collections.unmodifiableList(amici); }

    @Override
    public Ruolo haRuolo() { return Ruolo.STUDENTE; }
}