package org.project.ing.classifunzionali;

import org.project.model.Studente;
import org.project.model.VirtualWallet;

public class WalletBuilder {

    private WalletBuilder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static VirtualWallet build(VirtualWallet wallet, Studente studente){

        if(wallet == null || studente == null)
            throw new IllegalArgumentException("Wallet o Studente non possono essere nulli.");

        wallet.collegaStudente(studente);
        studente.assegnaWallet(wallet);

        return wallet;
    }
}
