package org.project.model;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Message {
    private final Utente mittente;
    private final Utente destinatario;
    private final String testo;
    private final LocalDateTime timestamp;


    public Message(Utente mittente, Utente destinatario, String testo) {
        this(mittente, destinatario, testo, LocalDateTime.now(ZoneId.systemDefault()));
    }

    public Message(Utente mittente, Utente destinatario, String testo, LocalDateTime timestampOriginale) {
        if (mittente == null || destinatario == null) {
            throw new IllegalArgumentException("Mittente e destinatario non possono essere nulli.");
        }
        if (testo == null || testo.trim().isEmpty()) {
            throw new IllegalArgumentException("Il testo del messaggio non può essere vuoto.");
        }
        this.mittente = mittente;
        this.destinatario = destinatario;
        this.testo = testo;
        this.timestamp = timestampOriginale;
    }

    public Utente getMittente() { return mittente; }
    public Utente getDestinatario() { return destinatario; }
    public String getTesto() { return testo; }
    public LocalDateTime getTimestamp() { return timestamp; }
}