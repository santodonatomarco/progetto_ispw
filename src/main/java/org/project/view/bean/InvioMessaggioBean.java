package org.project.view.bean;

public class InvioMessaggioBean {
    private final String destinatario;
    private final String testo;

    public InvioMessaggioBean(String destinatario, String testo) {
        if (destinatario == null || destinatario.isBlank() || !destinatario.contains("@")) {
            throw new IllegalArgumentException("La mail del destinatario non è corretta");
        }
        if (testo == null || testo.isBlank()) {
            throw new IllegalArgumentException("Non si può inviare un messaggio vuoto");
        }
        this.destinatario = destinatario;
        this.testo = testo;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getTesto() {
        return testo;
    }

}
