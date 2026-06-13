package org.project.dao.transazioni;

import org.project.exceptions.DAOException;
import org.project.model.Transaction;

import java.util.*;

public class TransactionDAODemo extends TransactionDAO {

    // email → lista transazioni per quell'utente
    private final Map<String, List<Transaction>> fintoDatabase = new HashMap<>();

    @Override
    protected void doSaveTransazione(String email, Transaction t) throws DAOException {
        fintoDatabase.computeIfAbsent(email, k -> new ArrayList<>()).add(t);
    }

    @Override
    protected void doUpdateTransazione(String email, Transaction t) throws DAOException {
        // L'oggetto è già aggiornato per riferimento — nulla da fare
    }

    @Override
    protected List<Transaction> doRetrieveTransazioniByEmail(String email) throws DAOException {
        return new ArrayList<>(fintoDatabase.getOrDefault(email, Collections.emptyList()));
    }

    @Override
    protected void doDeleteTransazioniByEmail(String email) throws DAOException {
        fintoDatabase.remove(email);  // rimuove solo le transazioni di questo utente
    }
}