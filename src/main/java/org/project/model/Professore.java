package org.project.model;

import org.project.ing.enumerations.AuthProvider;
import org.project.ing.enumerations.Ruolo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Professore extends Utente {

    private String passwordHash; // usato solo se authProvider == LOCAL, altrimenti ""
    private List<SchoolClass> classiInsegnate;

    public Professore(String email, String nome, String cognome, AuthProvider provider) {
        super(email, nome, cognome, provider);
        this.passwordHash = "";
        this.classiInsegnate = new ArrayList<>();
    }

    // ── Gestione password (solo LOCAL) ──

    public void impostaPasswordHash(String hash) {
        if (hash == null || hash.trim().isEmpty())
            throw new IllegalArgumentException("La password hash non può essere vuota.");
        this.passwordHash = hash;
    }

    public String getPasswordHash() {
        return passwordHash; }

    // ── Gestione classi ────

    public final void aggiungiClasse(SchoolClass classe) {
        if (classe == null)
            throw new IllegalArgumentException("La classe non può essere nulla.");
        if (!this.classiInsegnate.contains(classe))
            this.classiInsegnate.add(classe);
    }

    public final void assegnaClassi(List<SchoolClass> classi) {
        this.classiInsegnate = (classi != null) ? new ArrayList<>(classi) : new ArrayList<>();
    }

    public List<SchoolClass> presentaClassiInsegnate() {
        return Collections.unmodifiableList(classiInsegnate);
    }

    @Override
    public Ruolo haRuolo() { return Ruolo.PROFESSORE; }
}