package org.project.view;

import org.project.view.bean.PortafoglioBean;
import org.project.view.bean.StockBean;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Implementazione CLI della schermata Mercato.
 *
 * Funzionalità:
 *  - Ricerca stock per simbolo
 *  - Visualizzazione dettaglio (prezzo, variazioni, market cap)
 *  - Lista degli stock già monitorati nella sessione
 *  - Avvio acquisto — solo per gli studenti
 */
public class MercatoGraphicControllerCLI extends MercatoGraphicController {

    private final Scanner sc = new Scanner(System.in);
    private static final NumberFormat VALUTA =
            NumberFormat.getCurrencyInstance(Locale.ITALY);

    // Ultimo stock visualizzato nel dettaglio
    private StockBean stockCorrente;

    public MercatoGraphicControllerCLI(Navigator navigator) {
        super.setNavigator(navigator);
    }

    @Override
    public void start() {
        String ruolo = isStudente ? "Studente" : "Professore (sola lettura)";

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.printf( "║         UNIFINANCE — Mercato (%s)%n", ruolo);
        System.out.println("╚══════════════════════════════════════════════╝");

        if (!isStudente) {
            System.out.println("  ℹ  Modalità sola lettura — nessun acquisto disponibile.\n");
        } else {
            PortafoglioBean pf = navigator.getPortafoglio();
            if (pf != null) {
                System.out.printf("  💵 Saldo disponibile: %s%n%n",
                        VALUTA.format(pf.getSaldoDisponibile()));
            }
        }

        // Mostra stock già monitorati
        List<StockBean> lista = navigator.getListaStock();
        if (lista != null && !lista.isEmpty()) {
            System.out.println("  ── Stock monitorati ─────────────────────────");
            System.out.printf("  %-8s %-24s %10s %10s %10s%n",
                    "Simbolo", "Azienda", "Prezzo", "Var.Giorn.", "Settore");
            System.out.println("  " + "-".repeat(68));
            for (StockBean s : lista) {
                System.out.printf("  %-8s %-24s %10.2f %+9.2f%% %10s%n",
                        s.getSimbolo(),
                        tronca(s.getNomeAzienda(), 24),
                        s.getPrezzoAttuale(),
                        s.getVariazioneGiornaliera(),
                        tronca(s.getSettore(), 10));
            }
            System.out.println();
        }

        mostraMenu();
    }

    // ── Dettaglio stock ───────────────────────────────────────────────────────

    @Override
    protected void mostraDettaglioStock(StockBean s) {
        stockCorrente = s;

        System.out.println("\n  ── DETTAGLIO STOCK ──────────────────────────");
        System.out.printf("  %-18s %s  (%s)%n", s.getSimbolo(), s.getNomeAzienda(), s.getSettore());
        System.out.println("  " + "─".repeat(52));
        System.out.printf("  Prezzo attuale    : $ %.2f%n",         s.getPrezzoAttuale());
        System.out.printf("  Var. giornaliera  : %+.2f%%%n",        s.getVariazioneGiornaliera());
        System.out.printf("  Var. settimanale  : %+.2f%%%n",        s.getVariazioneSettimanale());
        System.out.printf("  Market Cap        : %s%n",             formatMarketCap(s.getMarketCap()));
        System.out.printf("  Volume sett.      : %.0f%n",           s.getVolumeSettimanale());

        if (isStudente) {
            PortafoglioBean pf = navigator.getPortafoglio();
            System.out.println();
            if (pf != null) {
                System.out.printf("  Saldo disponibile : %s%n",
                        VALUTA.format(pf.getSaldoDisponibile()));
                if (pf.getSaldoDisponibile() < s.getPrezzoAttuale()) {
                    System.out.println("  ⚠  Saldo insufficiente per acquistare questo titolo.");
                }
            }
        }

        mostraMenuDettaglio();
    }

    // ── Menu principale ───────────────────────────────────────────────────────

    private void mostraMenu() {
        System.out.println("  ── MENU ─────────────────────────────────────");
        System.out.println("  [C] Cerca stock per simbolo");
        System.out.println("  [R] Aggiorna lista");
        System.out.println("  [D] Torna alla Dashboard");
        System.out.println("  [0] Logout");
        System.out.print("\n  Scelta: ");

        String scelta = sc.nextLine().trim().toUpperCase();

        switch (scelta) {
            case "C" -> {
                System.out.print("  Simbolo (es. AAPL): ");
                String sym = sc.nextLine().trim();
                eseguiRicerca(sym);
            }
            case "R" -> start();
            case "D" -> tornaDashboard();
            case "0" -> eseguiLogout();
            default  -> {
                mostraErrore("Scelta non valida.");
                mostraMenu();
            }
        }
    }

    // ── Menu dopo aver visto il dettaglio ─────────────────────────────────────

    private void mostraMenuDettaglio() {
        System.out.println("\n  ── AZIONI ───────────────────────────────────");

        if (isStudente) {
            PortafoglioBean pf = navigator.getPortafoglio();
            boolean saldoSufficiente = pf == null || pf.getSaldoDisponibile() >= stockCorrente.getPrezzoAttuale();
            if (saldoSufficiente) {
                System.out.println("  [A] Acquista " + stockCorrente.getSimbolo());
            }
        }

        System.out.println("  [C] Cerca un altro stock");
        System.out.println("  [M] Torna alla lista mercato");
        System.out.println("  [D] Torna alla Dashboard");
        System.out.println("  [0] Logout");
        System.out.print("\n  Scelta: ");

        String scelta = sc.nextLine().trim().toUpperCase();

        switch (scelta) {
            case "A" -> {
                if (isStudente && stockCorrente != null) {
                    eseguiAvviaOrdine(stockCorrente.getSimbolo());
                } else {
                    mostraErrore("Scelta non valida.");
                    mostraMenuDettaglio();
                }
            }
            case "C" -> {
                System.out.print("  Simbolo (es. TSLA): ");
                String sym = sc.nextLine().trim();
                eseguiRicerca(sym);
            }
            case "M" -> start();
            case "D" -> tornaDashboard();
            case "0" -> eseguiLogout();
            default  -> {
                mostraErrore("Scelta non valida.");
                mostraMenuDettaglio();
            }
        }
    }

    // ── Feedback ─────────────────────────────────────────────────────────────

    @Override
    protected void mostraCaricamento(boolean visible) {
        if (visible) System.out.println("  ⏳ Recupero dati da Yahoo Finance…");
    }

    @Override
    protected void mostraErrore(String msg) {
        System.out.println("\n  !!! " + msg + " !!!\n");
    }

    @Override
    protected void showMessage(String msg) {
        System.out.println("\n  " + msg + "\n");
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String tronca(String s, int max) {
        if (s == null) return "—";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private String formatMarketCap(double cap) {
        if (cap <= 0) return "—";
        if (cap >= 1_000_000_000_000.0) return String.format("$ %.2f T", cap / 1_000_000_000_000.0);
        if (cap >= 1_000_000_000.0)     return String.format("$ %.2f B", cap / 1_000_000_000.0);
        if (cap >= 1_000_000.0)         return String.format("$ %.2f M", cap / 1_000_000.0);
        return VALUTA.format(cap);
    }
}