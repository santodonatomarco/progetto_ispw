package org.project.view.bean;

import java.util.List;


public class SchoolClassBean {

    private String nome;
    private ProfessoreBean professore;
    private double budgetIniziale;
    private List<StudenteBean> studenti;   // può essere null se non serve la lista completa

    // Costruttore "leggero" — senza lista studenti (es. per lo studente che vede solo la sua classe)
    public SchoolClassBean(String nome, ProfessoreBean professore, double budgetIniziale) {
        this.nome = nome;
        this.professore = professore;
        this.budgetIniziale = budgetIniziale;
    }

    // Costruttore "completo" — con lista studenti (es. per il professore che gestisce la classe)
    public SchoolClassBean(String nome, ProfessoreBean professore,
                           double budgetIniziale, List<StudenteBean> studenti) {
        this(nome, professore, budgetIniziale);
        this.studenti = studenti;
    }

    public String getNome()                         { return nome; }
    public ProfessoreBean getProfessore()           { return professore; }
    public double getBudgetIniziale()               { return budgetIniziale; }
    public List<StudenteBean> getStudenti()         { return studenti; }

    public void setNome(String nome)                                { this.nome = nome; }
    public void setProfessore(ProfessoreBean professore)            { this.professore = professore; }
    public void setBudgetIniziale(double budgetIniziale)            { this.budgetIniziale = budgetIniziale; }
    public void setStudenti(List<StudenteBean> studenti)            { this.studenti = studenti; }
}