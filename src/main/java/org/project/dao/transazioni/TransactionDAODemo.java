package org.project.dao.transazioni;

import org.project.exceptions.DAOException;
import org.project.model.Transaction;

import java.util.*;
import java.time.LocalDateTime;

public class TransactionDAODemo extends TransactionDAO {

    // email → lista transazioni per quell'utente
    private final Map<String, List<Transaction>> fintoDatabase = new HashMap<>();

    @Override
    protected void doSaveTransazione(String email, Transaction t) throws DAOException {
        fintoDatabase.computeIfAbsent(email, k -> new ArrayList<>()).add(t);
    }

    @Override
    protected void doUpdateTransazione(String email, Transaction t) throws DAOException {
        // L'oggetto è già aggiornato per riferimento
    }

    @Override
    protected void doUpdateTransazione(String email, Transaction t, LocalDateTime oldTimestamp) throws DAOException {
        // Cerca una transazione esistente con lo stesso simbolo e timestamp precedente
        List<Transaction> lista = fintoDatabase.get(email);
        if (lista == null) {
            // nessuna transazione esistente per questo utente: crea la lista e aggiungi
            fintoDatabase.computeIfAbsent(email, k -> new ArrayList<>()).add(t);
            return;
        }

        for (int i = 0; i < lista.size(); i++) {
            Transaction curr = lista.get(i);
            if (curr.stock().simbolo().equals(t.stock().simbolo()) && curr.quando().equals(oldTimestamp)) {
                lista.set(i, t);
                return;
            }
        }

        // se non trovata, aggiungila in coda
        lista.add(t);
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