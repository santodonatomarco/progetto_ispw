package org.project.model;

import java.time.LocalDateTime;

public class Message {
    private final Utente mittente;
    private final Utente destinatario;
    private final String testo;
    private final LocalDateTime timestamp;


    // 1. Costruttore per i NUOVI messaggi (usato dal Controller)
    public Message(Utente mittente, Utente destinatario, String testo) {
        // usa il timestamp attuale
        this(mittente, destinatario, testo, LocalDateTime.now());
    }

    // 2. Costruttore per i messaggi ESISTENTI (usato dal DAO)
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
        this.timestamp = timestampOriginale; // Usa la data passata dal DB!
    }

    public Utente getMittente() { return mittente; }
    public Utente getDestinatario() { return destinatario; }
    public String getTesto() { return testo; }
    public LocalDateTime getTimestamp() { return timestamp; }
}