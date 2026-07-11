package org.project.view.bean;

public class OttieniStudentiClasseBean {

    private final String nomeClasse;

    public OttieniStudentiClasseBean(String nomeClasse) {
        if (nomeClasse == null || nomeClasse.isBlank())
            throw new IllegalArgumentException("Nome classe non può essere vuoto.");
        this.nomeClasse = nomeClasse.trim();
    }

    public String getNomeClasse() {
        return nomeClasse;
    }
}

