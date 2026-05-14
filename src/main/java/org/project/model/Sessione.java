package org.project.model;

import java.util.List;

/**
 * Rappresenta il contesto attivo di un utente durante la sua sessione applicativa.
 * Viene creata dal SessionManager al momento del login e distrutta al logout.
 *
 * Mantiene in memoria tutto ciò che serve per navigare nell'app senza
 * rileggere continuamente il DB: l'utente loggato, il suo wallet,
 * la classe di appartenenza, i dati correntemente visualizzati, ecc.
 */
public class Sessione {

    /** Identificatore univoco di questa sessione (assegnato dal SessionManager). */
    private int token;

    // ── Utente loggato ────────────────────────────────────────────────────────
    // Solo uno dei due sarà non-null a seconda del ruolo.
    private Studente studenteCorrente;
    private Professore professorCorrente;

    // ── Dati "contestuali" caricati durante il flusso ─────────────────────────
    /** Wallet dello studente loggato (null se è un professore). */
    private VirtualWallet walletCorrente;

    /** Classe correntemente selezionata/visualizzata (utile sia per studente che professore). */
    private SchoolClass classeCorrente;

    /** Stock correntemente selezionato per operazioni di acquisto/vendita. */
    private Stock stockCorrente;

    /** Ranking correntemente visualizzato. */   // non so se la implemento
    private Ranking rankingCorrente;

    /** Elenco di stock presenti nel wallet, pre-caricati per non rileggere il DB. */
    private List<WalletPosition> posizioniCaricate;

    // ─────────────────────────────────────────────────────────────────────────
    // Getter / Setter
    // ─────────────────────────────────────────────────────────────────────────

    public int getToken()              { return token; }
    public void setToken(int token)    { this.token = token; }

    public Studente getStudenteCorrente()                         { return studenteCorrente; }
    public void setStudenteCorrente(Studente studenteCorrente)    { this.studenteCorrente = studenteCorrente; }

    public Professore getProfessorCorrente()                           { return professorCorrente; }
    public void setProfessorCorrente(Professore professorCorrente)     { this.professorCorrente = professorCorrente; }

    /**
     * Restituisce l'utente loggato indipendentemente dal ruolo.
     * Comodo nei controller che non hanno bisogno di distinguere Studente/Professore.
     */
    public Utente getUtenteCorrente() {
        if (studenteCorrente != null) return studenteCorrente;
        return professorCorrente;
    }

    public VirtualWallet getWalletCorrente()                          { return walletCorrente; }
    public void setWalletCorrente(VirtualWallet walletCorrente)       { this.walletCorrente = walletCorrente; }

    public SchoolClass getClasseCorrente()                            { return classeCorrente; }
    public void setClasseCorrente(SchoolClass classeCorrente)         { this.classeCorrente = classeCorrente; }

    public Stock getStockCorrente()                                   { return stockCorrente; }
    public void setStockCorrente(Stock stockCorrente)                 { this.stockCorrente = stockCorrente; }

    public Ranking getRankingCorrente()                               { return rankingCorrente; }
    public void setRankingCorrente(Ranking rankingCorrente)           { this.rankingCorrente = rankingCorrente; }

    public List<WalletPosition> getPosizioniCaricate()                          { return posizioniCaricate; }
    public void setPosizioniCaricate(List<WalletPosition> posizioniCaricate)    { this.posizioniCaricate = posizioniCaricate; }
}