package org.project.model;

import java.util.ArrayList;
import java.util.List;

public class SchoolClass {

    private String nome;
    private Professore teacher;
    private List<Studente> studenti;
    private List<Message> chat;

    public SchoolClass(String nome, Professore teacher) {
        this.battezzaClasse(nome);
        this.assegnaProfessore(teacher);
        this.studenti = new ArrayList<>();
        this.chat = new ArrayList<>();
    }

    public final void battezzaClasse(String nome) {
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Il nome della classe non può essere vuoto.");
        this.nome = nome;
    }

    public final void assegnaProfessore(Professore teacher) {
        if (teacher == null)
            throw new IllegalArgumentException("Il teacher non può essere nullo.");
        this.teacher = teacher;
    }

    public final void iscriviStudente(Studente s) {
        if (s == null)
            throw new IllegalArgumentException("Lo studente non può essere nullo.");
        this.studenti.add(s);
    }

    public final void aggiungiMessaggio(Message m) {
        if (m == null)
            throw new IllegalArgumentException("Il messaggio non può essere nullo.");
        this.chat.add(m);
    }

    public String nome()                { return nome; }
    public Professore teacher()            { return teacher; }
    public List<Studente> studenti()     { return studenti; }
    public List<Message> chat()         { return chat; }
}