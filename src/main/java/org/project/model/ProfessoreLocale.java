package org.project.model;

import org.project.ing.enumerations.AuthProvider;

public class ProfessoreLocale extends Professore implements AutenticazioneLocale {

    private String passwordHash;

    public ProfessoreLocale(String email, String nome, String cognome) {
        super(email, nome, cognome, AuthProvider.LOCAL);
    }

    @Override
    public final void inserisciHashPassword(String hash) {
        if (hash == null || hash.trim().isEmpty())
            throw new IllegalArgumentException("La password hash non può essere vuota.");
        this.passwordHash = hash;
    }

    @Override
    public String passwordHash() { return passwordHash; }

    @Override
    public AuthProvider comeAccede() { return AuthProvider.LOCAL; }
}
