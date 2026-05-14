package org.project.model;

import java.util.List;

/**
 * Rappresenta il contesto attivo di un utente durante la sua sessione applicativa.
 * Viene creata dal SessionManager al momento del login e distrutta al logout.
 */
public class Sessione {

    private int token;

    // ── Utente loggato ────────────────────────────────────────────────────────
    private Studente studenteCorrente;
    private Professore professorCorrente;

    // ── Dati contestuali ──────────────────────────────────────────────────────
    private VirtualWallet walletCorrente;
    private SchoolClass classeCorrente;
    private Stock stockCorrente;
    private Ranking rankingCorrente;
    private List<WalletPosition> posizioniCaricate;

    // ── Ordine in corso (timeout 5 minuti) ───────────────────────────────────
    /** Transazione PENDING creata da MercatoAppController, confermata da OrdineAppController. */
    private Transaction transazionePending;

    // ── Getter / Setter ───────────────────────────────────────────────────────

    public int getToken()                           { return token; }
    public void setToken(int token)                 { this.token = token; }

    public Studente getStudenteCorrente()                               { return studenteCorrente; }
    public void setStudenteCorrente(Studente studenteCorrente)          { this.studenteCorrente = studenteCorrente; }

    public Professore getProfessorCorrente()                            { return professorCorrente; }
    public void setProfessorCorrente(Professore professorCorrente)      { this.professorCorrente = professorCorrente; }

    public Utente getUtenteCorrente() {
        if (studenteCorrente != null) return studenteCorrente;
        return professorCorrente;
    }

    public VirtualWallet getWalletCorrente()                            { return walletCorrente; }
    public void setWalletCorrente(VirtualWallet walletCorrente)         { this.walletCorrente = walletCorrente; }

    public SchoolClass getClasseCorrente()                              { return classeCorrente; }
    public void setClasseCorrente(SchoolClass classeCorrente)           { this.classeCorrente = classeCorrente; }

    public Stock getStockCorrente()                                     { return stockCorrente; }
    public void setStockCorrente(Stock stockCorrente)                   { this.stockCorrente = stockCorrente; }

    public Ranking getRankingCorrente()                                 { return rankingCorrente; }
    public void setRankingCorrente(Ranking rankingCorrente)             { this.rankingCorrente = rankingCorrente; }

    public List<WalletPosition> getPosizioniCaricate()                  { return posizioniCaricate; }
    public void setPosizioniCaricate(List<WalletPosition> posizioni)    { this.posizioniCaricate = posizioni; }

    public Transaction getTransazionePending()                          { return transazionePending; }
    public void setTransazionePending(Transaction t)                    { this.transazionePending = t; }
}