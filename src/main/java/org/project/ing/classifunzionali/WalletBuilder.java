package org.project.ing.classifunzionali;

import org.project.model.Studente;
import org.project.model.VirtualWallet;

public class WalletBuilder {
    public static VirtualWallet build(VirtualWallet wallet, Studente studente){

        if(wallet == null || studente == null)
            throw new IllegalArgumentException("Wallet o Studente non possono essere nulli.");

        wallet.collegaStudente(studente);
        studente.assegnaWallet(wallet);

        return wallet;
    }
}
