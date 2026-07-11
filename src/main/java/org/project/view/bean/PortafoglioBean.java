package org.project.view.bean;

import java.util.List;


public class PortafoglioBean {

    private double saldoDisponibile;
    private double valoreTotalePortafoglio;   // saldo + valore di tutte le posizioni
    private List<WalletPositionBean> posizioni;
    private List<TransactionBean> transazioni;

    public PortafoglioBean(double saldoDisponibile, double valoreTotalePortafoglio,
                           List<WalletPositionBean> posizioni,
                           List<TransactionBean> transazioni) {
        this.saldoDisponibile = saldoDisponibile;
        this.valoreTotalePortafoglio = valoreTotalePortafoglio;
        this.posizioni = posizioni;
        this.transazioni = transazioni;
    }

    public double getSaldoDisponibile()             { return saldoDisponibile; }
    public double getValoreTotalePortafoglio()      { return valoreTotalePortafoglio; }
    public List<WalletPositionBean> getPosizioni()  { return posizioni; }
    public List<TransactionBean> getTransazioni()   { return transazioni; }

    public void setSaldoDisponibile(double saldo)                       { this.saldoDisponibile = saldo; }
    public void setValoreTotalePortafoglio(double valore)               { this.valoreTotalePortafoglio = valore; }
    public void setPosizioni(List<WalletPositionBean> posizioni)        { this.posizioni = posizioni; }
    public void setTransazioni(List<TransactionBean> transazioni)       { this.transazioni = transazioni; }
}