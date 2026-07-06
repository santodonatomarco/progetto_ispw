package org.project.view.bean;


public class ClasseBean {
    private final String nomeClasse;
    private final double budgetIniziale;

    public ClasseBean(String nomeClasse, double budgetIniziale) {
        if (nomeClasse == null || nomeClasse.isBlank())
            throw new IllegalArgumentException("Il nome della classe non può essere vuoto.");
        if (budgetIniziale < 0){
            throw new IllegalArgumentException("Il budget iniziale non può essere negativo.");
        }
        this.nomeClasse = nomeClasse;
        this.budgetIniziale = budgetIniziale;
    }

    public ClasseBean(String nomeClasse){
        if (nomeClasse == null || nomeClasse.isBlank())
            throw new IllegalArgumentException("Il nome della classe non può essere vuoto.");
        this.nomeClasse = nomeClasse;
        this.budgetIniziale = getBudgetIniziale();
    }


    public String getNomeDellaClasse() {
        return nomeClasse;
    }

    public double getBudgetIniziale() {
        return budgetIniziale;
    }

}
