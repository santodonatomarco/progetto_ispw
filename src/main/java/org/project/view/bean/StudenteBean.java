package org.project.view.bean;

import org.project.ing.enumerations.AuthProvider;

public class StudenteBean {

    private String email;
    private String nome;
    private String cognome;
    private String password;           // usata solo nel login/registrazione locale
    private AuthProvider authProvider; // null se locale
    private String nomeClasse;         // usata solo nella registrazione studente
    private double budgetClasse;       // <-- Aggiunto il campo per il budget

    // Costruttore per login/registrazione locale
    public StudenteBean(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Costruttore per visualizzazione (post-login)
    public StudenteBean(String email, String nome, String cognome) {
        this.email = email;
        this.nome = nome;
        this.cognome = cognome;
    }

    // ── Getters ─────────────────────────────────────────────────────────────
    public String getEmail()                { return email; }
    public String getNome()                 { return nome; }
    public String getCognome()              { return cognome; }
    public String getPassword()             { return password; }
    public AuthProvider getAuthProvider()   { return authProvider; }
    public String getNomeClasse()           { return nomeClasse; }
    public double getBudgetClasse()         { return budgetClasse; } // <-- Getter aggiunto

    // ── Setters ─────────────────────────────────────────────────────────────
    public void setNome(String nome)                        { this.nome = nome; }
    public void setCognome(String cognome)                  { this.cognome = cognome; }
    public void setAuthProvider(AuthProvider authProvider)  { this.authProvider = authProvider; }
    public void setNomeClasse(String nomeClasse)            { this.nomeClasse = nomeClasse; }

    // <-- Setter completato per ricevere il parametro
    public void setBudgetClasse(double budgetClasse)        { this.budgetClasse = budgetClasse; }

    public void resetPassword()                             { this.password = ""; }
}