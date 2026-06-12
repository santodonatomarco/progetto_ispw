package org.project.dao.transazioni;

import org.project.exceptions.DAOException;
import org.project.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionDAODemo extends TransactionDAO {

    // email_studente → lista transazioni
    private final List<Transaction> fintoDatabase = new ArrayList<>();

    @Override
    protected void doSaveTransazione(String email, Transaction t) throws DAOException {
        fintoDatabase.add(t);
    }

    @Override
    protected void doUpdateTransazione(String email, Transaction t) throws DAOException {
        // In memoria l'oggetto è già aggiornato per riferimento — nulla da fare
    }

    @Override
    protected List<Transaction> doRetrieveTransazioniByEmail(String email) throws DAOException {
        // Nella demo non filtriamo per email — restituiamo tutto
        return new ArrayList<>(fintoDatabase);
    }

    @Override
    protected void doDeleteTransazioniByEmail(String email) throws DAOException {
        // La demo non filtra per email (fintoDatabase non mantiene l'email).
        // Se vuoi essere preciso, aggiungi email al modello demo o svuota tutto.
        fintoDatabase.clear();
    }


}