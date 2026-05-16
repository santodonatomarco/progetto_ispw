package org.project.model;

import org.project.ing.enumerations.AuthProvider;

public class ProfessoreOAuth extends Professore implements AutenticazioneOAuth {

    public ProfessoreOAuth(String email, String nome, String cognome, AuthProvider provider) {
        super(email, nome, cognome, validaProvider(provider));
    }

    private static AuthProvider validaProvider(AuthProvider provider) {
        if (provider == null || provider == AuthProvider.LOCAL)
            throw new IllegalArgumentException("Provider OAuth non valido.");
        return provider;
    }

    @Override
    public AuthProvider comeAccede() {
        return ottieniAuthProvider();
    }

    @Override
    public AuthProvider ottieniProvider() {
        return ottieniAuthProvider();
    }
}
