package org.project.view.bean;

public class AggiungiStudenteBean {

    private final String emailStudente;
    private final String nomeClasse;

    public AggiungiStudenteBean(String emailStudente, String nomeClasse) {
        if (emailStudente == null || emailStudente.isBlank())
            throw new IllegalArgumentException("Email non può essere vuota.");
        if (!emailStudente.contains("@"))
            throw new IllegalArgumentException("Email non valida.");
        if (nomeClasse == null || nomeClasse.isBlank())
            throw new IllegalArgumentException("Nome classe non può essere vuoto.");

        this.emailStudente = emailStudente.trim().toLowerCase();
        this.nomeClasse = nomeClasse.trim();
    }

    public String getEmailStudente() {
        return emailStudente;
    }

    public String getNomeClasse() {
        return nomeClasse;
    }
}

