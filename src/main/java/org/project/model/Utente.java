package org.project.model;
import org.project.ing.enumerations.AuthProvider;
import org.project.ing.enumerations.Ruolo;


public abstract class Utente {

    private String email;
    private String nome;
    private String cognome;
    private String profilePicture;
    private AuthProvider authProvider;

    protected Utente(String email, String nome, String cognome, AuthProvider provider) {
        this.impostaEmail(email);
        this.chiamaNome(nome);
        this.chiamaCognome(cognome);
        this.authProvider = (provider != null) ? provider : AuthProvider.LOCAL;
    }

    public final void impostaEmail(String email) {
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Email non valida.");
        this.email = email;
    }

    public final void chiamaNome(String nome) {
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Il nome non può essere vuoto.");
        this.nome = nome;
    }

    public final void chiamaCognome(String cognome) {
        if (cognome == null || cognome.trim().isEmpty())
            throw new IllegalArgumentException("Il cognome non può essere vuoto.");
        this.cognome = cognome;
    }

    public String presentaEmail()   { return email; }
    public String presentaNome()    { return nome; }
    public String presentaCognome() { return cognome; }
    public String fotoProfilo()     { return profilePicture; }
    public void impostaFotoProfilo(String url) { this.profilePicture = url; }

    public AuthProvider comeAccede() { return authProvider; }

    public abstract Ruolo haRuolo();
}