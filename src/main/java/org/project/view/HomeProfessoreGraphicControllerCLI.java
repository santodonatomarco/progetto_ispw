package org.project.view;

import org.project.view.bean.ProfessoreBean;
import org.project.view.bean.SchoolClassBean;

import java.util.List;
import java.util.Scanner;

/**
 * Implementazione CLI della schermata Home del Professore.
 * Mostra le classi gestite e permette di navigare al mercato (sola lettura)
 * o alla gestione di una classe specifica.
 */
public class HomeProfessoreGraphicControllerCLI extends HomeProfessoreGraphicController {

    private final Scanner sc = new Scanner(System.in);

    public HomeProfessoreGraphicControllerCLI(Navigator navigator) {
        this.setNavigator(navigator);
    }

    @Override
    public void start() {
        ProfessoreBean professore = getProfessoreLoggato();
        if (professore == null) {
            mostraMessaggio("Sessione non valida. Effettua nuovamente il login.");
            navigator.goToLogin();
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║      UNIFINANCE — Dashboard Professore       ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.printf("  Prof. %s %s%n", professore.getNome(), professore.getCognome());
        System.out.printf("  Email: %s%n", professore.getEmail());
        System.out.println("\n  ℹ  Modalità professore: accesso al mercato in sola lettura.");
        System.out.println("     Non è possibile effettuare investimenti.");

        List<SchoolClassBean> classi = getListaClassi();
        aggiornaUIClassi(classi);

        mostraMenu(classi);
    }

    // ── aggiornaUIClassi ──────────────────────────────────────────────────────

    @Override
    protected void aggiornaUIClassi(List<SchoolClassBean> classi) {
        System.out.println("\n  ── LE MIE CLASSI ────────────────────────────");
        if (classi == null || classi.isEmpty()) {
            System.out.println("  Nessuna classe assegnata.");
            return;
        }

        int totStudenti = 0;
        for (SchoolClassBean c : classi) {
            if (c.getStudenti() != null) totStudenti += c.getStudenti().size();
        }
        System.out.printf("  Classi gestite: %d  |  Studenti totali: %d%n",
                classi.size(), totStudenti);
        System.out.println("  " + "-".repeat(52));

        for (int i = 0; i < classi.size(); i++) {
            SchoolClassBean c = classi.get(i);
            int numStudenti = (c.getStudenti() != null) ? c.getStudenti().size() : 0;
            System.out.printf("  [%d] %-20s  Budget: € %10.2f  Studenti: %d%n",
                    i + 1,
                    c.getNome(),
                    c.getBudgetIniziale(),
                    numStudenti);
        }
    }

    // ── Menu navigazione ──────────────────────────────────────────────────────

    private void mostraMenu(List<SchoolClassBean> classi) {
        System.out.println("\n  ── MENU ─────────────────────────────────────");
        System.out.println("  [M] Visualizza Mercato (sola lettura)");

        boolean haClassi = (classi != null && !classi.isEmpty());
        if (haClassi) {
            System.out.println("  [numero classe] Gestisci quella classe");
        }

        System.out.println("  [G] Gestione Classi");
        System.out.println("  [I] Inbox / Messaggi");
        System.out.println("  [R] Aggiorna dashboard");
        System.out.println("  [0] Logout");
        System.out.print("\n  Scelta: ");

        String scelta = sc.nextLine().trim().toUpperCase();

        switch (scelta) {
            case "M" -> vaiAlMercato();
            case "G" -> vaiAGestioneClasse();
            case "I" -> vaiAllaInbox();
            case "R" -> start();
            case "0" -> eseguiLogout();
            default  -> {
                // Selezione numerica di una classe
                if (haClassi) {
                    try {
                        int idx = Integer.parseInt(scelta) - 1;
                        if (idx >= 0 && idx < classi.size()) {
                            navigator.impostaClasseCorrente(classi.get(idx));
                            vaiAGestioneClasse();
                            return;
                        }
                    } catch (NumberFormatException ignored) {
                        // eccezione ignorata, verrà mostrato il messaggio di scelta non valida
                    }
                }
                mostraMessaggio("Scelta non valida.");
                mostraMenu(classi);
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
}