package org.project.model;

import java.time.LocalDateTime;

public class Message {

    private Utente mittente;
    private String testo;
    private LocalDateTime timestamp;
    private boolean letto;

    public Message(Utente mittente, String testo) {
        this.impostaMittente(mittente);
        this.scriviTesto(testo);
        this.timestamp = LocalDateTime.now();
        this.letto = false;
    }

    public final void impostaMittente(Utente mittente) {
        if (mittente == null)
            throw new IllegalArgumentException("Il mittente non può essere nullo.");
        this.mittente = mittente;
    }

    public final void scriviTesto(String testo) {
        if (testo == null || testo.trim().isEmpty())
            throw new IllegalArgumentException("Il messaggio non può essere vuoto.");
        this.testo = testo;
    }

    public final void segnaComeLetto() {
        this.letto = true;
    }

    public Utente mittente()          { return mittente; }
    public String testo()           { return testo; }
    public LocalDateTime quando()   { return timestamp; }
    public boolean isRead()         { return letto; }
}