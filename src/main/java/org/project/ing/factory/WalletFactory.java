package org.project.ing.factory;

import org.project.model.Studente;
import org.project.model.SchoolClass;
import org.project.model.VirtualWallet;

public class WalletFactory {

    /**
     * Crea un nuovo portafoglio per lo studente, prelevando
     * automaticamente il budget iniziale stabilito dal professore per quella classe.
     */
    public static VirtualWallet creaWalletPerStudente(Studente studente) {
        if (studente == null) {
            throw new IllegalArgumentException("Lo studente non può essere nullo");
        }

        SchoolClass classe = studente.classeFrequentata();

        // Se lo studente non è in nessuna classe, diamo budget 0 o lanciamo eccezione?
        // Supponiamo che debba essere iscritto per avere soldi.
        double saldoIniziale = 0.0; // deve avere una classe per essere iscritto
        if (classe != null) {
            saldoIniziale = classe.budgetIniziale();
        }

        return new VirtualWallet(studente, saldoIniziale);
    }
}