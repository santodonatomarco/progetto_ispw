package org.project.view;

import org.project.view.bean.PortafoglioBean;
import org.project.view.bean.StudenteBean;
import org.project.view.bean.TransactionBean;
import org.project.view.bean.WalletPositionBean;

import java.util.List;
import java.util.Scanner;


public class HomeStudenteGraphicControllerCLI extends HomeStudenteGraphicController {

    private final Scanner sc = new Scanner(System.in);

    public HomeStudenteGraphicControllerCLI(Navigator navigator) {
        this.setNavigator(navigator);
    }

    @Override
    public void start() {
        StudenteBean studente = getStudenteLoggato();
        if (studente == null) {
            mostraMessaggio("Sessione non valida. Effettua nuovamente il login.");
            navigator.goToLogin();
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║       UNIFINANCE — Dashboard Studente        ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.printf("  Ciao, %s %s!%n", studente.getNome(), studente.getCognome());
        System.out.printf("  Email: %s%n", studente.getEmail());

        String nomeClasse = (studente.getNomeClasse() != null) ? studente.getNomeClasse() : "—";
        System.out.printf("  Classe: %s%n", nomeClasse);

        PortafoglioBean portafoglio = getPortafoglio();
        if (portafoglio != null) {
            aggiornaUIPortafoglio(portafoglio);
        } else {
            System.out.println("\n  (Portafoglio non disponibile)");
        }

        mostraMenu();
    }

    // ── aggiornaUIPortafoglio ─────────────────────────────────────────────────

    // ── aggiornaUIPortafoglio ─────────────────────────────────────────────────

    @Override
    protected void aggiornaUIPortafoglio(PortafoglioBean portafoglio) {
        System.out.println("\n  ── PORTAFOGLIO ──────────────────────────────");
        System.out.printf("  Saldo disponibile : € %.2f%n", portafoglio.getSaldoDisponibile());
        System.out.printf("  Valore totale     : € %.2f%n", portafoglio.getValoreTotalePortafoglio());

        mostraPosizioni(portafoglio.getPosizioni());
        mostraTransazioni(portafoglio.getTransazioni());
    }

    private void mostraPosizioni(List<WalletPositionBean> posizioni) {
        System.out.println("\n  ── POSIZIONI APERTE ─────────────────────────");

        if (posizioni == null || posizioni.isEmpty()) {
            System.out.println("  Nessuna posizione aperta.");
            return;
        }

        System.out.printf("  %-8s %-20s %8s %12s %12s %10s%n",
                "Simbolo", "Azienda", "Qtà", "P.Medio", "Valore att.", "P/L");
        System.out.println("  " + "-".repeat(76));

        for (WalletPositionBean p : posizioni) {
            String pl = String.format("%+.2f €", p.getProfittoPerdita());
            System.out.printf("  %-8s %-20s %8.2f %12.2f %12.2f %10s%n",
                    p.getStock().getSimbolo(),
                    tronca(p.getStock().getNomeAzienda(), 20),
                    p.getQuantita(),
                    p.getPrezzoMedioAcquisto(),
                    p.getValoreAttuale(),
                    pl);
        }
    }

    private void mostraTransazioni(List<TransactionBean> transazioni) {
        System.out.println("\n  ── ULTIME TRANSAZIONI ───────────────────────");

        if (transazioni == null || transazioni.isEmpty()) {
            System.out.println("  Nessuna transazione ancora effettuata.");
            return;
        }

        int limite = Math.min(5, transazioni.size());
        System.out.printf("  %-8s %-6s %12s %-10s%n",
                "Simbolo", "Tipo", "Importo", "Stato");
        System.out.println("  " + "-".repeat(42));

        for (int i = 0; i < limite; i++) {
            TransactionBean tx = transazioni.get(i);
            System.out.printf("  %-8s %-6s %12.2f %-10s%n",
                    tx.getStock().getSimbolo(),
                    tx.getTipo() != null ? tx.getTipo().name() : "—",
                    tx.getImportoTotale(),
                    tx.getStato() != null ? tx.getStato().name() : "—");
        }
    }

    // ── Menu navigazione ──────────────────────────────────────────────────────

    private void mostraMenu() {
        System.out.println("\n  ── MENU ─────────────────────────────────────");
        System.out.println("  [1] Vai al Mercato");
        System.out.println("  [2] Vai al Portafoglio");
        System.out.println("  [3] Storico Ordini");
        System.out.println("  [4] Inbox / Messaggi");
        System.out.println("  [R] Aggiorna dashboard");
        System.out.println("  [0] Logout");
        System.out.print("\n  Scelta: ");

        String scelta = sc.nextLine().trim().toUpperCase();

        switch (scelta) {
            case "1" -> vaiAlMercato();
            case "2" -> vaiAlPortafoglio();
            case "3" -> vaiAlloStorico();
            case "4" -> vaiAllaInbox();
            case "R" -> start();
            case "0" -> eseguiLogout();
            default  -> {
                mostraMessaggio("Scelta non valida.");
                mostraMenu();
            }
        }
    }

    // ── Feedback ─────────────────────────────────────────────────────────────

    @Override
    protected void mostraMessaggio(String msg) {
        System.out.println("\n!!! " + msg + " !!!\n");
    }

    @Override
    protected void showMessage(String msg) {
        System.out.println("\n" + msg + "\n");
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String tronca(String s, int max) {
        if (s == null) return "—";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}