package org.project.model;

import org.project.ing.enumerations.AuthProvider;
import org.project.ing.enumerations.Ruolo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Studente extends Utente {

    private String passwordHash; // valorizzato solo se authProvider == LOCAL, altrimenti ""
    private SchoolClass schoolClass;
    private VirtualWallet wallet;
    private List<Utente> amici;
    private List<Alert> alerts;

    public Studente(String email, String nome, String cognome, AuthProvider provider) {
        super(email, nome, cognome, provider);
        this.passwordHash = "";
        this.amici = new ArrayList<>();
        this.alerts = new ArrayList<>();
    }

    // ── Gestione password (solo LOCAL) ────────────────────────────────────────

    public void impostaPasswordHash(String hash) {
        if (hash == null || hash.trim().isEmpty())
            throw new IllegalArgumentException("La password hash non può essere vuota.");
        this.passwordHash = hash;
    }

    public String getPasswordHash() { return passwordHash; }

    // ── Gestione classe/wallet/amici/alert ───────────────────────────────────

    public final void iscriviClasse(SchoolClass classe) {
        if (classe == null)
            throw new IllegalArgumentException("La classe non può essere nulla.");
        this.schoolClass = classe;
    }

    public final void assegnaWallet(VirtualWallet wallet) {
        if (wallet == null)
            throw new IllegalArgumentException("Il wallet non può essere nullo.");
        this.wallet = wallet;
    }

    public final void aggiungAmico(Utente amico) {
        if (amico == null || amico.equals(this))
            throw new IllegalArgumentException("Amico non valido.");
        if (!this.amici.contains(amico))
            this.amici.add(amico);
    }

    public final void riceviAlert(Alert alert) {
        if (alert == null)
            throw new IllegalArgumentException("L'alert non può essere nullo.");
        this.alerts.add(alert);
    }

    public SchoolClass classeFrequentata() { return schoolClass; }
    public VirtualWallet portafoglio()     { return wallet; }
    public List<Utente> presentaAmici()    { return Collections.unmodifiableList(amici); }
    public List<Alert> presentaAlert()     { return Collections.unmodifiableList(alerts); }

    @Override
    public Ruolo haRuolo() { return Ruolo.STUDENTE; }
}