package org.project.view;

import java.util.Scanner;


public class LoginGraphicControllerCLI extends LoginGraphicController {

    private final Scanner sc = new Scanner(System.in);

    public LoginGraphicControllerCLI(Navigator navigator) {
        this.setNavigator(navigator);
    }

    @Override
    public void start() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║         UNIFINANCE — Login       ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.println();
        System.out.println("  [1] Accedi come Studente");
        System.out.println("  [2] Accedi come Professore");
        System.out.println("  [3] Registrati come nuovo Studente");
        System.out.println("  [4] Registrati come nuovo Professore");
        System.out.println("  [0] Esci");
        System.out.print("\nScelta: ");

        String scelta = sc.nextLine().trim();

        switch (scelta) {
            case "1" -> {
                super.isStudente = true;
                leggiCredenziali();
                super.eseguiLogin();
            }
            case "2" -> {
                super.isStudente = false;
                leggiCredenziali();
                super.eseguiLogin();
            }
            case "3" -> navigator.goToRegistrazione();
            case "4" -> navigator.goToRegistrazione();
            case "0" -> chiudiApp();
            default  -> {
                mostraErrore("Scelta non valida.");
                start();
            }
        }
    }

    private void leggiCredenziali() {
        System.out.print("Email: ");
        super.email = sc.nextLine().trim();

        System.out.print("Password: ");
        super.password = sc.nextLine().trim();
    }

    @Override
    protected void mostraErrore(String msg) {
        System.out.println("\n!!! " + msg.toUpperCase() + " !!!\n");
        start();
    }

    @Override
    protected void showMessage(String msg) {
        System.out.println("\n" + msg + "\n");
    }
}