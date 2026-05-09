package org.project.model;

import java.time.LocalDateTime;

public class Alert {

    private StudenteOAuth destinatario;
    private Stock stock;
    private double profittoPerdita;     // valore giornaliero +/-
    private LocalDateTime inviatoAlle;
    private boolean visualizzato;

    public Alert(StudenteOAuth destinatario, Stock stock, double profittoPerdita) {
        this.impostaDestinatario(destinatario);
        this.collegaStock(stock);
        this.registraProfittoPerdita(profittoPerdita);
        this.inviatoAlle = LocalDateTime.now();
        this.visualizzato = false;
    }

    public final void impostaDestinatario(StudenteOAuth destinatario) {
        if (destinatario == null)
            throw new IllegalArgumentException("Il destinatario non può essere nullo.");
        this.destinatario = destinatario;
    }

    public final void collegaStock(Stock stock) {
        if (stock == null)
            throw new IllegalArgumentException("Lo stock non può essere nullo.");
        this.stock = stock;
    }

    public final void registraProfittoPerdita(double valore) {
        this.profittoPerdita = valore;  // può essere negativo — è una perdita
    }

    public final void segnaVisualizzato() {
        this.visualizzato = true;
    }

    public StudenteOAuth destinatario()       { return destinatario; }
    public Stock stock()                { return stock; }
    public double profittoPerdita()     { return profittoPerdita; }
    public LocalDateTime quando()       { return inviatoAlle; }
    public boolean èVisualizzato()      { return visualizzato; }
}