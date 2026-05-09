package org.project.model;

public interface AutenticazioneLocale {
    void inserisciHashPassword(String hash);
    String passwordHash();
}

