package org.project.view;

public enum Schermate {

    // ── Comuni ────────────────────────────────────────────────────────────────
    LOGIN,
    REGISTRAZIONE,

    // ── Studente ──────────────────────────────────────────────────────────────
    HOME_STUDENTE,
    MERCATO,            // lista/ricerca stock
    DETTAGLIO_STOCK,    // dati di uno stock + pulsante "Compra"
    CONFERMA_ORDINE,    // inserimento quantità + conferma acquisto
    PORTAFOGLIO,        // riepilogo wallet e posizioni aperte
    STORICO,            // storico transazioni

    // ── Professore ────────────────────────────────────────────────────────────
    HOME_PROFESSORE,
    GESTIONE_CLASSE,    // crea classe, aggiunge studenti pending
    ELENCO_STUDENTI,    // lista studenti della classe

    // ── Comune studente/professore ────────────────────────────────────────────
    RANKING             // classifica portafogli della classe
}