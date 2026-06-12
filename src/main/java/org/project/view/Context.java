package org.project.view;

import org.project.view.bean.*;

import java.util.List;

/**
 * Contenitore dello stato di navigazione.
 * Vive dentro il Navigator e conserva tutti i bean condivisi tra schermate.
 * Nessun oggetto model — solo bean.
 */
public class Context {

    // ── Sessione e utente loggato ─────────────────────────────────────────────
    private SessioneBean    sessione;
    private StudenteBean    studenteLoggato;
    private ProfessoreBean  professoreLoggato;

    // ── Dati mercato ──────────────────────────────────────────────────────────
    private StockBean        stockCorrente;
    private List<StockBean>  listaStock;

    // ── Ordine in corso ───────────────────────────────────────────────────────
    private TransactionBean  transazionePending;

    // ── Portafoglio ───────────────────────────────────────────────────────────
    private PortafoglioBean        portafoglio;
    private List<TransactionBean>  storicoTransazioni;

    // ── Portafoglio esterno (WALLET_STUDENTE) ─────────────────────────────────
    /**
     * Studente di cui visualizzare il portafoglio (non proprietario).
     * Impostato prima di goToWalletStudente(); null = nessuna navigazione esterna attiva.
     */
    private StudenteBean studenteTarget;

    // ── Classe e studenti (uso professore e studente della stessa classe) ───────────────────────────────────
    private SchoolClassBean        classeCorrente;
    private List<StudenteBean>     studentiClasse;
    private List<SchoolClassBean>  listaClassi;

    // ─────────────────────────────────────────────────────────────────────────

    public Context() {}

    // ── Sessione e utente ─────────────────────────────────────────────────────

    public SessioneBean   getSessione()                              { return sessione; }
    public void           setSessione(SessioneBean s)                { this.sessione = s; }

    public StudenteBean   getStudenteLoggato()                       { return studenteLoggato; }
    public void           setStudenteLoggato(StudenteBean s)         { this.studenteLoggato = s; }

    public ProfessoreBean getProfessoreLoggato()                     { return professoreLoggato; }
    public void           setProfessoreLoggato(ProfessoreBean p)     { this.professoreLoggato = p; }

    // ── Mercato ───────────────────────────────────────────────────────────────

    public StockBean       getStockCorrente()                        { return stockCorrente; }
    public void            setStockCorrente(StockBean s)             { this.stockCorrente = s; }

    public List<StockBean> getListaStock()                           { return listaStock; }
    public void            setListaStock(List<StockBean> l)          { this.listaStock = l; }

    // ── Ordine ────────────────────────────────────────────────────────────────

    public TransactionBean  getTransazionePending()                  { return transazionePending; }
    public void             setTransazionePending(TransactionBean t) { this.transazionePending = t; }

    // ── Portafoglio ───────────────────────────────────────────────────────────

    public PortafoglioBean       getPortafoglio()                    { return portafoglio; }
    public void                  setPortafoglio(PortafoglioBean p)   { this.portafoglio = p; }

    public List<TransactionBean> getStoricoTransazioni()             { return storicoTransazioni; }
    public void                  setStoricoTransazioni(List<TransactionBean> l) { this.storicoTransazioni = l; }

    // ── Portafoglio esterno ───────────────────────────────────────────────────

    public StudenteBean getStudenteTarget()                          { return studenteTarget; }
    public void         setStudenteTarget(StudenteBean s)            { this.studenteTarget = s; }

    // ── Classe e studenti ─────────────────────────────────────────────────────

    public SchoolClassBean        getClasseCorrente()                { return classeCorrente; }
    public void                   setClasseCorrente(SchoolClassBean c){ this.classeCorrente = c; }

    public List<StudenteBean>     getStudentiClasse()                { return studentiClasse; }
    public void                   setStudentiClasse(List<StudenteBean> l) { this.studentiClasse = l; }

    public List<SchoolClassBean>  getListaClassi()                   { return listaClassi; }
    public void                   setListaClassi(List<SchoolClassBean> l) { this.listaClassi = l; }
}