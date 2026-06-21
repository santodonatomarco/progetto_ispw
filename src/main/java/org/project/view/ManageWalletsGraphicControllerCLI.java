package org.project.view;

import org.project.view.bean.*;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Implementazione CLI del controller grafico ManageWallets.
 * Gestisce in un unico controller tutti i sotto-flussi testuali:
 * – Browsing mercato (ricerca, lista monitorati)
 * – Dettaglio stock + avvio acquisto
 * – Conferma / annullamento ordine
 * – Portafoglio proprio (posizioni + saldo)
 * – Storico transazioni proprio
 * – Portafoglio esterno in sola lettura (studente/professore non proprietario)
 */
public class ManageWalletsGraphicControllerCLI extends ManageWalletsGraphicController {

    private static final NumberFormat VALUTA = NumberFormat.getCurrencyInstance(Locale.ITALY);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Costanti per risolvere i warning sui duplicati dei literal (SonarQube/SonarLint)
    private static final String BORDER_TOP = "╔══════════════════════════════════════════════╗";
    private static final String BORDER_BOTTOM = "╚══════════════════════════════════════════════╝";
    private static final String LBL_SIMBOLO = "Simbolo";
    private static final String MSG_SCELTA_NON_VALIDA = "Scelta non valida.";
    private static final String LBL_TORNA_DASHBOARD = "Torna alla Dashboard";
    private static final String LBL_VAI_MERCATO = "Vai al Mercato";
    private static final String OPT_ZERO = "  [0] ";
    private static final String PROMPT_SCELTA = "  Scelta: ";
    private static final String MENU_DIVIDER = "  ─────────────────────────────────────────────";

    private final Scanner sc = new Scanner(System.in);

    public ManageWalletsGraphicControllerCLI(Navigator navigator) {
        super.setNavigator(navigator);
    }

    // ── Punti di ingresso ─────────────────────────────────────────────────────

    @Override
    public void start() {
        String ruolo = isStudente ? "Studente" : "Professore (sola lettura)";
        System.out.println("\n" + BORDER_TOP);
        System.out.printf( "║     UNIFINANCE — Mercato  (%s)%n", ruolo);
        System.out.println(BORDER_BOTTOM);

        if (!isStudente) {
            System.out.println("  ℹ  Modalità sola lettura — nessun acquisto disponibile.\n");
        } else {
            PortafoglioBean pf = navigator.getPortafoglio();
            if (pf != null)
                System.out.printf("  💵 Saldo disponibile: %s%n%n",
                        VALUTA.format(pf.getSaldoDisponibile()));
        }

        List<StockBean> lista = navigator.getListaStock();
        if (lista != null && !lista.isEmpty()) {
            System.out.println("  ── Stock monitorati ──────────────────────────");
            System.out.printf("  %-8s %-24s %10s %10s%n",
                    LBL_SIMBOLO, "Azienda", "Prezzo", "Var.Giorn.");
            System.out.println("  " + "─".repeat(58));
            for (StockBean s : lista) {
                System.out.printf("  %-8s %-24s %10.2f %+9.2f%%%n",
                        s.getSimbolo(), tronca(s.getNomeAzienda(), 24),
                        s.getPrezzoAttuale(), s.getVariazioneGiornaliera());
            }
            System.out.println();
        }

        mostraMenuPrincipale();
    }

    @Override
    public void startConfermaOrdine() {
        TransactionBean t = navigator.getTransazionePending();
        if (t == null) {
            mostraErrore("Nessun ordine da confermare. Torna al mercato.");
            navigator.goToMercato();
            return;
        }

        System.out.println("\n" + BORDER_TOP);
        System.out.println("║        UNIFINANCE — Conferma Ordine          ║");
        System.out.println(BORDER_BOTTOM);
        System.out.printf("  Stock:   %s — %s%n",
                t.getStock().getSimbolo(), t.getStock().getNomeAzienda());
        System.out.printf("  Prezzo:  %s / azione%n",
                VALUTA.format(t.getPrezzoAlMomento()));

        PortafoglioBean pf = navigator.getPortafoglio();
        if (pf != null)
            System.out.printf("  Saldo:   %s disponibili%n%n",
                    VALUTA.format(pf.getSaldoDisponibile()));

        System.out.print("  Inserisci la quantità di azioni (es. 2.5): ");
        String input = sc.nextLine().trim();

        double quantita;
        try {
            quantita = Double.parseDouble(input.replace(",", "."));
            if (quantita <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostraErrore("Quantità non valida. Inserisci un numero positivo.");
            navigator.goToConfermaOrdine();
            return;
        }

        System.out.printf("%n  Totale stimato: %s%n",
                VALUTA.format(quantita * t.getPrezzoAlMomento()));
        System.out.print("  Confermi l'acquisto? [S/N]: ");
        String scelta = sc.nextLine().trim().toUpperCase();

        if ("S".equals(scelta)) {
            eseguiConfermaAcquisto(quantita);
        } else {
            eseguiAnnullaOrdine();
        }
    }

    @Override
    public void startPortafoglio() {
        System.out.println("\n" + BORDER_TOP);
        System.out.println("║        UNIFINANCE — Il Tuo Portafoglio       ║");
        System.out.println(BORDER_BOTTOM);
        eseguiCaricaPortafoglio(null);
    }

    @Override
    public void startStorico() {
        System.out.println("\n" + BORDER_TOP);
        System.out.println("║    UNIFINANCE — Storico Transazioni          ║");
        System.out.println(BORDER_BOTTOM);
        eseguiCaricaStorico(null);
    }

    @Override
    public void startWalletEsterno(StudenteBean studenteTarget) {
        String nome = studenteTarget.getNome() + " " + studenteTarget.getCognome();
        System.out.println("\n" + BORDER_TOP);
        System.out.printf( "║  Portafoglio di: %-28s║%n", tronca(nome, 28));
        System.out.println(BORDER_BOTTOM);
        System.out.println("  (visualizzazione in sola lettura)\n");
        eseguiCaricaPortafoglio(studenteTarget.getEmail());
    }

    // ── Visualizzazione portafoglio ───────────────────────────────────────────

    @Override
    protected void mostraPortafoglio(PortafoglioBean pf, boolean isProprietario) {
        if (pf == null) {
            System.out.println("  Nessun dato di portafoglio disponibile.");
            mostraMenuPortafoglio(isProprietario);
            return;
        }

        System.out.printf("  💵 Saldo disponibile:     %s%n",
                VALUTA.format(pf.getSaldoDisponibile()));
        System.out.printf("  📊 Valore totale wallet:  %s%n",
                VALUTA.format(pf.getValoreTotalePortafoglio()));

        List<WalletPositionBean> posizioni = pf.getPosizioni();
        if (posizioni == null || posizioni.isEmpty()) {
            System.out.println("\n  Nessuna posizione aperta.");
        } else {
            System.out.println("\n  ── Posizioni aperte ───────────────────────────────────────────");
            System.out.printf("  %-8s %-20s %10s %12s %12s %12s%n",
                    LBL_SIMBOLO, "Azienda", "Quantità", "Pr.Medio", "Val.Att.", "P/L");
            System.out.println("  " + "─".repeat(80));
            for (WalletPositionBean p : posizioni) {
                String pl = p.getProfittoPerdita() >= 0
                        ? "+" + VALUTA.format(p.getProfittoPerdita())
                        : VALUTA.format(p.getProfittoPerdita());
                System.out.printf("  %-8s %-20s %10.4f %12.2f %12s %12s%n",
                        p.getStock().getSimbolo(),
                        tronca(p.getStock().getNomeAzienda(), 20),
                        p.getQuantita(),
                        p.getPrezzoMedioAcquisto(),
                        VALUTA.format(p.getValoreAttuale()),
                        pl);
            }
        }

        mostraMenuPortafoglio(isProprietario);
    }


    @Override
    protected void mostraStorico(List<TransactionBean> storico, String emailTarget) {
        if (storico == null || storico.isEmpty()) {
            System.out.println("\n  Nessuna transazione registrata.");
            mostraMenuStorico(emailTarget == null);
            return;
        }

        System.out.printf("%n  ── Transazioni (%d) ─────────────────────────────────────────%n",
                storico.size());
        System.out.printf("  %-12s %-8s %-8s %10s %10s  %-16s%n",
                "Data", LBL_SIMBOLO, "Tipo", "Quantità", "Importo", "Stato");
        System.out.println("  " + "─".repeat(72));

        for (TransactionBean t : storico) {
            stampaRigaTransazione(t);
        }

        mostraMenuStorico(emailTarget == null);
    }

    private void stampaRigaTransazione(TransactionBean t) {
        String data = t.getQuando() != null ? t.getQuando().format(FMT) : "—";
        String simbolo = t.getStock() != null ? t.getStock().getSimbolo() : "?";
        String tipo = t.getTipo() != null ? t.getTipo().name() : "?";
        String stato = t.getStato() != null ? t.getStato().name() : "?";

        System.out.printf("  %-12s %-8s %-8s %10.4f %10s  %-16s%n",
                data,
                simbolo,
                tipo,
                t.getQuantita(),
                VALUTA.format(t.getImportoTotale()),
                stato);
    }


    private void mostraMenuPrincipale() {
        System.out.println(MENU_DIVIDER);
        System.out.println("  [1] Cerca uno stock");
        if (isStudente) {
            System.out.println("  [2] Vai al Portafoglio");
            System.out.println("  [3] Storico transazioni");
        }
        System.out.println(OPT_ZERO + LBL_TORNA_DASHBOARD);
        System.out.print(PROMPT_SCELTA);

        switch (sc.nextLine().trim()) {
            case "1" -> {
                System.out.print("  Simbolo (es. AAPL): ");
                eseguiRicerca(sc.nextLine().trim());
            }
            case "2" -> {
                if (isStudente) navigator.goToPortafoglio();
                else mostraMenuPrincipale();
            }
            case "3" -> {
                if (isStudente) navigator.goToStorico();
                else mostraMenuPrincipale();
            }
            case "0" -> tornaDashboard();
            default  -> { mostraErrore(MSG_SCELTA_NON_VALIDA); mostraMenuPrincipale(); }
        }
    }

    private void mostraMenuPortafoglio(boolean isProprietario) {
        System.out.println("\n" + MENU_DIVIDER);
        if (isProprietario) {
            System.out.println("  [1] Storico transazioni");
            System.out.println("  [2] " + LBL_VAI_MERCATO);
        }
        System.out.println(OPT_ZERO + LBL_TORNA_DASHBOARD);
        System.out.print(PROMPT_SCELTA);

        switch (sc.nextLine().trim()) {
            case "1" -> { if (isProprietario) navigator.goToStorico(); else tornaDashboard(); }
            case "2" -> { if (isProprietario) navigator.goToMercato(); else tornaDashboard(); }
            case "0" -> tornaDashboard();
            default  -> { mostraErrore(MSG_SCELTA_NON_VALIDA); mostraMenuPortafoglio(isProprietario); }
        }
    }

    private void mostraMenuStorico(boolean isProprietario) {
        System.out.println("\n" + MENU_DIVIDER);
        if (isProprietario) {
            System.out.println("  [1] Vai al Portafoglio");
            System.out.println("  [2] " + LBL_VAI_MERCATO);
        }
        System.out.println(OPT_ZERO + LBL_TORNA_DASHBOARD);
        System.out.print(PROMPT_SCELTA);

        switch (sc.nextLine().trim()) {
            case "1" -> { if (isProprietario) navigator.goToPortafoglio(); else tornaDashboard(); }
            case "2" -> { if (isProprietario) navigator.goToMercato(); else tornaDashboard(); }
            case "0" -> tornaDashboard();
            default  -> { mostraErrore(MSG_SCELTA_NON_VALIDA); mostraMenuStorico(isProprietario); }
        }
    }

    // ── Dettaglio stock (chiamato da eseguiRicerca) ───────────────────────────

    @Override
    protected void mostraDettaglioStock(StockBean s) {
        System.out.println("\n  ── Dettaglio: " + s.getSimbolo() + " ─────────────────────────");
        System.out.printf("  Azienda:     %s (%s)%n", s.getNomeAzienda(), s.getSettore());
        System.out.printf("  Prezzo:      $ %.4f%n",  s.getPrezzoAttuale());
        System.out.printf("  Var.giorn.:  %+.2f%%%n", s.getVariazioneGiornaliera());
        System.out.printf("  Var.sett.:   %+.2f%%%n", s.getVariazioneSettimanale());

        System.out.println("\n  [1] Compra" + (isStudente ? "" : " (disabilitato — sola lettura)"));
        System.out.println(OPT_ZERO + "Torna al mercato");
        System.out.print(PROMPT_SCELTA);

        String scelta = sc.nextLine().trim();
        if ("1".equals(scelta) && isStudente) {
            eseguiAvviaOrdine(s.getSimbolo());
        } else {
            navigator.goToMercato();
        }
    }

    // ── Feedback ──────────────────────────────────────────────────────────────

    @Override
    protected void mostraCaricamento(boolean visible) {
        if (visible) System.out.println("  ⏳ Caricamento in corso...");
    }

    @Override
    protected void mostraErrore(String msg) {
        System.out.println("  ⚠  " + msg);
    }

    @Override
    protected void showMessage(String msg) {
        System.out.println("  ℹ  " + msg);
    }

    @Override
    protected void mostraAcquistoCompletato(TransactionBean t) {
        System.out.println("\n  ✅ Acquisto completato!");
        System.out.printf("  Stock:      %s%n", t.getStock().getSimbolo());
        System.out.printf("  Quantità:   %.4f azioni%n", t.getQuantita());
        System.out.printf("  Pr.unitar.: $ %.4f%n", t.getPrezzoAlMomento());
        System.out.printf("  Totale:     %s%n", VALUTA.format(t.getImportoTotale()));
        navigator.goToMercato();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String tronca(String s, int max) {
        if (s == null) return "—";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}