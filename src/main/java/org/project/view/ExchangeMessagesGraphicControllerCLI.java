package org.project.view;

import org.project.view.bean.MessageBean;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ExchangeMessagesGraphicControllerCLI extends ExchangeMessagesGraphicController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Scanner sc = new Scanner(System.in);

    public ExchangeMessagesGraphicControllerCLI(Navigator navigator) {
        this.setNavigator(navigator);
    }

    @Override
    public void start() {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║         UNIFINANCE — Inbox                   ║");
        System.out.println("╚══════════════════════════════════════════════╝");

        List<MessageBean> inbox = eseguiCaricaInbox();
        stampaInbox(inbox);
        mostraMenu();
    }

    // ── Visualizzazione inbox ─────────────────────────────────────────────────

    private void stampaInbox(List<MessageBean> inbox) {
        System.out.println("\n  ── MESSAGGI RICEVUTI ─────────────────────────");
        if (inbox == null || inbox.isEmpty()) {
            System.out.println("  Nessun messaggio ricevuto.");
            return;
        }

        System.out.println("  " + "-".repeat(66));
        for (int i = 0; i < inbox.size(); i++) {
            MessageBean m = inbox.get(i);
            String data = (m.getTimestamp() != null) ? m.getTimestamp().format(FMT) : "—";
            System.out.printf("  [%d] Da: %-30s  %s%n",
                    i + 1, m.getNominativoMittente(), data);
            System.out.printf("      (%s)%n", m.getEmailMittente());
            System.out.println("      " + tronca(m.getTesto(), 70));
            System.out.println("  " + "-".repeat(66));
        }
        System.out.printf("  Totale: %d messaggio/i%n", inbox.size());
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    private void mostraMenu() {
        System.out.println("\n  ── MENU ─────────────────────────────────────");
        System.out.println("  [N] Nuovo messaggio");
        System.out.println("  [R] Aggiorna inbox");
        System.out.println("  [D] Torna alla Dashboard");
        System.out.println("  [0] Logout");
        System.out.print("\n  Scelta: ");

        String scelta = sc.nextLine().trim().toUpperCase();
        switch (scelta) {
            case "N" -> flussoInviaMessaggio();
            case "R" -> start();
            case "D" -> tornaDashboard();
            case "0" -> eseguiLogout();
            default  -> {
                mostraErrore("Scelta non valida.");
                mostraMenu();
            }
        }
    }

    // ── Flusso invio messaggio ────────────────────────────────────────────────

    private void flussoInviaMessaggio() {
        System.out.println("\n  ── NUOVO MESSAGGIO ──────────────────────────");

        // Per gli studenti pre-compila l'email del professore
        String emailPrecompilata = getEmailProfessore();

        String emailDestinatario;
        if (emailPrecompilata != null) {
            System.out.printf("  A (professore — invio per confermare): %s%n", emailPrecompilata);
            System.out.print("  Oppure digita un'altra email (invio per usare quella sopra): ");
            String input = sc.nextLine().trim();
            emailDestinatario = input.isEmpty() ? emailPrecompilata : input;
        } else {
            System.out.print("  A (email destinatario): ");
            emailDestinatario = sc.nextLine().trim();
        }

        if (emailDestinatario.isEmpty()) {
            mostraErrore("Email destinatario obbligatoria.");
            start();
            return;
        }

        System.out.print("  Testo del messaggio (su una riga): ");
        String testo = sc.nextLine().trim();
        if (testo.isEmpty()) {
            mostraErrore("Il testo non può essere vuoto.");
            flussoInviaMessaggio();
            return;
        }

        System.out.println("\n  ── RIEPILOGO ────────────────────────────────");
        System.out.println("  A: " + emailDestinatario);
        System.out.println("  Testo: " + tronca(testo, 60));
        System.out.print("  Confermi l'invio? [S/n]: ");

        String conferma = sc.nextLine().trim().toUpperCase();
        if (!conferma.isEmpty() && !conferma.equals("S")) {
            System.out.println("  Invio annullato.");
            start();
            return;
        }

        MessageBean inviato = eseguiInviaMessaggio(emailDestinatario, testo);
        if (inviato != null) {
            start(); // ricarica inbox aggiornata
        } else {
            mostraMenu();
        }
    }

    // ── Feedback ─────────────────────────────────────────────────────────────

    @Override
    protected void mostraSuccesso(String msg) {
        System.out.println("\n  ✓ " + msg + "\n");
    }

    @Override
    protected void mostraErrore(String msg) {
        System.out.println("\n  ✗ " + msg + "\n");
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String tronca(String s, int max) {
        if (s == null) return "—";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
