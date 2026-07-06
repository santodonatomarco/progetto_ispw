package org.project.view.bean;

public class ConfermaAcquistoBean {
    private final double quantitaScelta;
    public ConfermaAcquistoBean(double quantitaScelta) {
        if (quantitaScelta <= 0)
            throw new IllegalArgumentException("La quantità deve essere maggiore di 0");
        this.quantitaScelta = quantitaScelta;
    }
    public double getQuantitaScelta() {
        return quantitaScelta;
    }
}
