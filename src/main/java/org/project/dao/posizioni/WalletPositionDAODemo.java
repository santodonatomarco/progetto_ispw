package org.project.dao.posizioni;

import org.project.exceptions.DAOException;
import org.project.model.WalletPosition;

import java.util.ArrayList;
import java.util.List;

public class WalletPositionDAODemo extends WalletPositionDAO {

    private final List<WalletPosition> fintoDatabase = new ArrayList<>();

    @Override
    protected void doSavePosizione(String email, WalletPosition p) throws DAOException {
        fintoDatabase.add(p);
    }

    @Override
    protected void doUpdatePosizione(String email, WalletPosition p) throws DAOException {
        // In memoria l'oggetto è già aggiornato per riferimento
    }

    @Override
    protected void doDeletePosizione(String email, WalletPosition p) throws DAOException {
        fintoDatabase.removeIf(pos -> pos.stock().simbolo().equals(p.stock().simbolo()));
    }

    @Override
    protected List<WalletPosition> doRetrievePosizioniByEmail(String email) throws DAOException {
        return new ArrayList<>(fintoDatabase);
    }

    @Override
    protected void doDeletePosizioniByEmail(String email) throws DAOException {
        fintoDatabase.clear();
    }



}