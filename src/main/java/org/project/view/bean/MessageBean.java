package org.project.view.bean;

import java.time.LocalDateTime;

public class MessageBean {

    private String emailMittente;
    private String nominativoMittente;
    private String emailDestinatario;
    private String nominativoDestinatario;
    private String testo;
    private LocalDateTime timestamp;

    // Costruttore vuoto (standard per i Bean)
    public MessageBean() {}

    public MessageBean(String emailMittente, String nominativoMittente,
                       String emailDestinatario, String nominativoDestinatario,
                       String testo, LocalDateTime timestamp) {
        this.emailMittente = emailMittente;
        this.nominativoMittente = nominativoMittente;
        this.emailDestinatario = emailDestinatario;
        this.nominativoDestinatario = nominativoDestinatario;
        this.testo = testo;
        this.timestamp = timestamp;
    }

    // ── Getters ─────────────────────────────────────────────────────────────
    public String getEmailMittente() { return emailMittente; }
    public String getNominativoMittente() { return nominativoMittente; }
    public String getEmailDestinatario() { return emailDestinatario; }
    public String getNominativoDestinatario() { return nominativoDestinatario; }
    public String getTesto() { return testo; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // ── Setters ─────────────────────────────────────────────────────────────
    public void setEmailMittente(String emailMittente) { this.emailMittente = emailMittente; }
    public void setNominativoMittente(String nominativoMittente) { this.nominativoMittente = nominativoMittente; }
    public void setEmailDestinatario(String emailDestinatario) { this.emailDestinatario = emailDestinatario; }
    public void setNominativoDestinatario(String nominativoDestinatario) { this.nominativoDestinatario = nominativoDestinatario; }
    public void setTesto(String testo) { this.testo = testo; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}