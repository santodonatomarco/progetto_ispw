package org.project.view.bean;

/**
 * Bean per una singola posizione nel wallet (un titolo posseduto).
 * Corrisponde a WalletPosition nel model.
 */
public class WalletPositionBean {

    private StockBean stock;
    private double quantita;
    private double prezzoMedioAcquisto;
    private double valoreAttuale;        // quantita * prezzoAttuale stock
    private double profittoPerdita;      // (prezzoAttuale - prezzoMedio) * quantita

    public WalletPositionBean(StockBean stock, double quantita,
                              double prezzoMedioAcquisto, double valoreAttuale,
                              double profittoPerdita) {
        this.stock = stock;
        this.quantita = quantita;
        this.prezzoMedioAcquisto = prezzoMedioAcquisto;
        this.valoreAttuale = valoreAttuale;
        this.profittoPerdita = profittoPerdita;
    }

    public StockBean getStock()                 { return stock; }
    public double getQuantita()                 { return quantita; }
    public double getPrezzoMedioAcquisto()      { return prezzoMedioAcquisto; }
    public double getValoreAttuale()            { return valoreAttuale; }
    public double getProfittoPerdita()          { return profittoPerdita; }

    public void setStock(StockBean stock)                       { this.stock = stock; }
    public void setQuantita(double quantita)                    { this.quantita = quantita; }
    public void setPrezzoMedioAcquisto(double prezzo)           { this.prezzoMedioAcquisto = prezzo; }
    public void setValoreAttuale(double valoreAttuale)          { this.valoreAttuale = valoreAttuale; }
    public void setProfittoPerdita(double profittoPerdita)      { this.profittoPerdita = profittoPerdita; }
}