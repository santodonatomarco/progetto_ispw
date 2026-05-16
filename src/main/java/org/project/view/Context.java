package org.project.view;

import org.project.view.bean.*;

import java.util.List;

/**
 * Contenitore dello stato della navigazione.
 * Vive dentro il Navigator e conserva tutti i bean che servono
 * tra una schermata e l'altra — nessun oggetto model, solo bean.
 */
public class Context {

    // ── Sessione e utente loggato ─────────────────────────────────────────────
    private SessioneBean sessione;
    private StudenteBean studenteLoggato;
    private ProfessoreBean professoreLoggato;

    // ── Dati mercato ──────────────────────────────────────────────────────────
    private StockBean stockCorrente;           // stock selezionato per analisi/acquisto
    private List<StockBean> listaStock;        // lista stock visualizzata nel mercato

    // ── Ordine in corso ───────────────────────────────────────────────────────
    private TransactionBean transazionePending; // ordine avviato, in attesa di conferma

    // ── Portafoglio ───────────────────────────────────────────────────────────
    private PortafoglioBean portafoglio;        // snapshot del wallet dello studente

    // ── Classe e studenti (uso professore) ───────────────────────────────────
    private SchoolClassBean classeCorrente;
    private List<StudenteBean> studentiClasse;
    private List<SchoolClassBean> listaClassi;

    // ── Storico transazioni ───────────────────────────────────────────────────
    private List<TransactionBean> storicoTransazioni;

    // ─────────────────────────────────────────────────────────────────────────
    // Costruttori
    // ─────────────────────────────────────────────────────────────────────────

    public Context() {}

    public Context(StudenteBean studente) {
        this.studenteLoggato = studente;
    }

    public Context(ProfessoreBean professore) {
        this.professoreLoggato = professore;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sessione e utente
    // ─────────────────────────────────────────────────────────────────────────

    public SessioneBean getSessione()                   { return sessione; }
    public void setSessione(SessioneBean sessione)      { this.sessione = sessione; }

    public StudenteBean getStudenteLoggato()                        { return studenteLoggato; }
    public void setStudenteLoggato(StudenteBean studente)           { this.studenteLoggato = studente; }

    public ProfessoreBean getProfessoreLoggato()                    { return professoreLoggato; }
    public void setProfessoreLoggato(ProfessoreBean professore)     { this.professoreLoggato = professore; }

    // ─────────────────────────────────────────────────────────────────────────
    // Mercato
    // ─────────────────────────────────────────────────────────────────────────

    public StockBean getStockCorrente()                     { return stockCorrente; }
    public void setStockCorrente(StockBean stock)           { this.stockCorrente = stock; }

    public List<StockBean> getListaStock()                  { return listaStock; }
    public void setListaStock(List<StockBean> lista)        { this.listaStock = lista; }

    // ─────────────────────────────────────────────────────────────────────────
    // Ordine in corso
    // ─────────────────────────────────────────────────────────────────────────

    public TransactionBean getTransazionePending()                          { return transazionePending; }
    public void setTransazionePending(TransactionBean transazione)          { this.transazionePending = transazione; }

    // ─────────────────────────────────────────────────────────────────────────
    // Portafoglio
    // ─────────────────────────────────────────────────────────────────────────

    public PortafoglioBean getPortafoglio()                     { return portafoglio; }
    public void setPortafoglio(PortafoglioBean portafoglio)     { this.portafoglio = portafoglio; }

    public List<TransactionBean> getStoricoTransazioni()                        { return storicoTransazioni; }
    public void setStoricoTransazioni(List<TransactionBean> storico)            { this.storicoTransazioni = storico; }

    // ─────────────────────────────────────────────────────────────────────────
    // Classe e studenti (uso professore)
    // ─────────────────────────────────────────────────────────────────────────

    public SchoolClassBean getClasseCorrente()                      { return classeCorrente; }
    public void setClasseCorrente(SchoolClassBean classe)           { this.classeCorrente = classe; }

    public List<StudenteBean> getStudentiClasse()                   { return studentiClasse; }
    public void setStudentiClasse(List<StudenteBean> studenti)      { this.studentiClasse = studenti; }

    public List<SchoolClassBean> getListaClassi()                   { return listaClassi; }
    public void setListaClassi(List<SchoolClassBean> classi)        { this.listaClassi = classi; }
}