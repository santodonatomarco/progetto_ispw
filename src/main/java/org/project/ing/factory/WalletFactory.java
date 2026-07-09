package org.project.ing.factory;

import org.project.model.Studente;
import org.project.model.SchoolClass;
import org.project.model.VirtualWallet;

public class WalletFactory {

    private WalletFactory() {
        throw new IllegalStateException("Utility class - Non istanziare");
    }

    /**
     * Crea un nuovo portafoglio per lo studente, prelevando
     * automaticamente il budget iniziale stabilito dal professore per quella classe.
     */
    public static VirtualWallet creaWalletPerStudente(Studente studente) {
        if (studente == null) {
            throw new IllegalArgumentException("Lo studente non può essere nullo");
        }

        SchoolClass classe = studente.classeFrequentata();

        double saldoIniziale = 0.0; // deve avere una classe per essere iscritto, viene impostato a 0 se non ha una classe
        if (classe != null) {
            saldoIniziale = classe.budgetIniziale();
        }

        return new VirtualWallet(studente, saldoIniziale);
    }
}