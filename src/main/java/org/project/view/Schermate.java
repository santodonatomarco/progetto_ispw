package org.project.view;

public enum Schermate {

    // ── Comuni ────────────────────────────────────────────────────────────────
    LOGIN,
    REGISTRAZIONE,

    // ── Studente ──────────────────────────────────────────────────────────────
    HOME_STUDENTE,
    MERCATO,            // lista/ricerca stock  (ManageWallets.start)
    DETTAGLIO_STOCK,    // dati uno stock       (ManageWallets — gestito internamente)
    CONFERMA_ORDINE,    // conferma acquisto    (ManageWallets.startConfermaOrdine)
    PORTAFOGLIO,        // wallet proprio       (ManageWallets.startPortafoglio)
    STORICO,            // storico transazioni  (ManageWallets.startStorico)

    // ── Non-proprietario (studente stessa classe / professore) ────────────────
    WALLET_STUDENTE,    // wallet altrui in sola lettura (ManageWallets.startWalletEsterno)

    // ── Professore ────────────────────────────────────────────────────────────
    HOME_PROFESSORE,
    GESTIONE_CLASSE,    // crea classe, aggiunge studenti pending

    // ── Messaggistica (comune a studente e professore) ────────────────────────
    INBOX,              // scambio messaggi studente ↔ professore
}