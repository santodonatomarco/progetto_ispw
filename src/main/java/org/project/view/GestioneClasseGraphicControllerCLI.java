package org.project.view;

import org.project.view.bean.SchoolClassBean;
import org.project.view.bean.StudenteBean;

import java.util.List;
import java.util.Scanner;

/**
 * Implementazione CLI della schermata Gestione Classe (professore).
 * Permette di:
 *  - Visualizzare le classi esistenti con budget
 *  - Selezionare una classe e modificarne il budget
 *  - Creare una nuova classe
 */
public class GestioneClasseGraphicControllerCLI extends GestioneClasseGraphicController {

    private final Scanner sc = new Scanner(System.in);

    public GestioneClasseGraphicControllerCLI(Navigator navigator) {
        this.setNavigator(navigator);
    }

    @Override
    public void start() {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║      UNIFINANCE — Gestione Classi            ║");
        System.out.println("╚══════════════════════════════════════════════╝");

        List<SchoolClassBean> classi = caricaClassi();
        stampaClassi(classi);
        mostraMenu(classi);
    }

    // ── Visualizzazione ───────────────────────────────────────────────────────

    private void stampaClassi(List<SchoolClassBean> classi) {
        System.out.println("\n  ── LE TUE CLASSI ────────────────────────────");
        if (classi == null || classi.isEmpty()) {
            System.out.println("  Nessuna classe trovata.");
            return;
        }
        System.out.println("  " + "-".repeat(52));
        for (int i = 0; i < classi.size(); i++) {
            SchoolClassBean c = classi.get(i);
            int numStudenti = (c.getStudenti() != null) ? c.getStudenti().size() : 0;
            System.out.printf("  [%d] %-20s  Budget: € %10.2f  Studenti: %d%n",
                    i + 1, c.getNome(), c.getBudgetIniziale(), numStudenti);
        }
        System.out.println("  " + "-".repeat(52));
        System.out.println("  → digita il numero della classe per gestirla");
        System.out.println("    (budget · aggiungi studente · elenco studenti)");
    }

    // ── Menu ─────────────────────────────────────────────────────────────────

    private void mostraMenu(List<SchoolClassBean> classi) {
        System.out.println("\n  ── MENU ─────────────────────────────────────");
        if (classi != null && !classi.isEmpty())
            System.out.println("  [numero] Gestisci la classe (budget, studenti...)");
        System.out.println("  [N] Nuova classe");
        System.out.println("  [R] Aggiorna lista");
        System.out.println("  [M] Vai al Mercato");
        System.out.println("  [D] Dashboard");
        System.out.println("  [0] Logout");
        System.out.print("\n  Scelta: ");

        String scelta = sc.nextLine().trim().toUpperCase();

        switch (scelta) {
            case "N" -> flussoNuovaClasse();
            case "R" -> start();
            case "M" -> vaiAlMercato();
            case "D" -> tornaDashboard();
            case "0" -> eseguiLogout();
            default  -> {
                if (classi != null && !classi.isEmpty()) {
                    try {
                        int idx = Integer.parseInt(scelta) - 1;
                        if (idx >= 0 && idx < classi.size()) {
                            flussoModificaBudget(classi.get(idx));
                            return;
                        }
                    } catch (NumberFormatException ignored) {
                        // eccezione ignorata
                    }
                }
                mostraErrore("Scelta non valida.");
                mostraMenu(classi);
            }
        }
    }

    // ── Flussi operativi ──────────────────────────────────────────────────────

    private void flussoNuovaClasse() {
        System.out.println("\n  ── NUOVA CLASSE ─────────────────────────────");

        System.out.print("  Nome classe (es. 3A): ");
        String nome = sc.nextLine().trim();
        if (nome.isEmpty()) {
            mostraErrore("Nome non può essere vuoto.");
            start();
            return;
        }

        double budget = leggiImporto("  Budget iniziale (€, invio per 0): ", 0.0);

        SchoolClassBean nuova = eseguiCreaClasse(nome, budget);
        if (nuova != null) {
            navigator.impostaClasseCorrente(nuova);
            // Ricarica la lista aggiornata di classi nella sessione
            List<SchoolClassBean> classiAggiornate = caricaClassi();
            if (classiAggiornate != null) {
                navigator.getSessione().setListaClassi(classiAggiornate);
            }
        }
        start();
    }

    private void flussoModificaBudget(SchoolClassBean classe) {
        System.out.println("\n  ── CLASSE: " + classe.getNome() + " ─────────────────────────");
        System.out.printf("  Budget attuale: € %.2f%n", classe.getBudgetIniziale());
        int numStudenti = (classe.getStudenti() != null) ? classe.getStudenti().size() : 0;
        System.out.println("  Studenti iscritti: " + numStudenti);

        System.out.println("\n  ── AZIONI ───────────────────────────────────");
        System.out.println("  [B] Modifica budget");
        System.out.println("  [A] Aggiungi studente alla classe");
        System.out.println("  [S] Visualizza elenco studenti");
        System.out.println("  [I] Indietro");
        System.out.print("\n  Scelta: ");

        String scelta = sc.nextLine().trim().toUpperCase();
        switch (scelta) {
            case "B" -> {
                double nuovoBudget = leggiImporto("  Nuovo budget (€): ", -1);
                if (nuovoBudget < 0) { mostraErrore("Budget non valido."); start(); return; }
                SchoolClassBean aggiornata = eseguiImpostaBudget(classe.getNome(), nuovoBudget);
                if (aggiornata != null) {
                    List<SchoolClassBean> classiAggiornate = caricaClassi();
                    if (classiAggiornate != null) navigator.getSessione().setListaClassi(classiAggiornate);
                }
                start();
            }
            case "A" -> {
                System.out.print("  Email dello studente da aggiungere: ");
                String email = sc.nextLine().trim();
                if (!email.isEmpty()) {
                    eseguiAggiungiStudente(email, classe.getNome());
                }
                flussoModificaBudget(classe); // torna al menu della classe
            }
            case "S" -> flussoElencoStudenti(classe);
            case "I" -> start();
            default  -> { mostraErrore("Scelta non valida."); flussoModificaBudget(classe); }
        }
    }

    private void flussoElencoStudenti(SchoolClassBean classe) {
        System.out.println("\n  ── STUDENTI DELLA CLASSE: " + classe.getNome() + " ─────────────────");
        List<StudenteBean> studenti = eseguiCaricaStudenti(classe.getNome());
        if (studenti == null) {
            // errore già mostrato da eseguiCaricaStudenti
            flussoModificaBudget(classe);
            return;
        }
        if (studenti.isEmpty()) {
            System.out.println("  Nessuno studente iscritto a questa classe.");
        } else {
            System.out.println("  " + "-".repeat(62));
            System.out.printf("  %-3s  %-26s %-26s%n", "#", "Nome", "Email");
            System.out.println("  " + "-".repeat(62));
            for (int i = 0; i < studenti.size(); i++) {
                StudenteBean s = studenti.get(i);
                String nomeCompleto = s.getNome() + " " + s.getCognome();
                System.out.printf("  %-3d  %-26s %-26s%n",
                        i + 1,
                        tronca(nomeCompleto, 26),
                        s.getEmail());
            }
            System.out.println("  " + "-".repeat(62));
            System.out.printf("  Totale: %d studenti%n", studenti.size());
        }
        System.out.println("\n  Premi Invio per tornare...");
        sc.nextLine();
        flussoModificaBudget(classe);
    }

    // ── Utility input ─────────────────────────────────────────────────────────

    /**
     * Legge un importo dalla console. Restituisce {@code defaultValue} se l'input è vuoto.
     * Restituisce -1 se il valore non è un numero valido.
     */
    private double leggiImporto(String prompt, double defaultValue) {
        System.out.print(prompt);
        String input = sc.nextLine().trim().replace(",", ".");
        if (input.isEmpty()) return defaultValue;
        try {
            double val = Double.parseDouble(input);
            return val >= 0 ? val : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ── Feedback ─────────────────────────────────────────────────────────────

    private String tronca(String s, int max) {
        if (s == null) return "—";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    @Override
    protected void mostraSuccesso(String msg) {
        System.out.println("\n  ✓ " + msg + "\n");
    }

    @Override
    protected void mostraErrore(String msg) {
        System.out.println("\n  ✗ " + msg + "\n");
    }
}