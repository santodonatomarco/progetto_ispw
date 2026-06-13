package org.project.dao.wallets;

import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.DAOException;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.factory.StockFactory;
import org.project.ing.factory.WalletFactory;
import org.project.model.*;

public class PortafoglioDAODemo extends PortafoglioDAO {

    private StudenteDAO studenteDAO;
    private final StockFactory stockFactory;

    public PortafoglioDAODemo(StockFactory stockFactory, StudenteDAO studenteDAO) {
        this.stockFactory = stockFactory;
        this.studenteDAO = studenteDAO;
    }

    @Override
    protected VirtualWallet doRetrievePortafoglioByEmail(String mail) throws DAOException {

        Studente s = studenteDAO.getStudenteByEmail(mail);
        if (s == null) {
            return null;
        }

        VirtualWallet wallet = WalletFactory.creaWalletPerStudente(s);

        try {
            Stock apple = stockFactory.creaStock("AAPL");

            // 1. Creazione e salvataggio POSIZIONE
            WalletPosition p = new WalletPosition(apple, 10, 180.0);
            wallet.aggiungiPosizione(p);

            if (walletPositionDAO != null) {
                walletPositionDAO.salvaPosizione(s.presentaEmail(),p);
            }

            Transaction t = new Transaction(
                    apple,
                    TipoTransazione.BUY,
                    10,
                    180.0
            );
            t.completaTransazione();
            wallet.aggiungiTransazione(t);

            if (transactionDAO != null) {
                transactionDAO.salvaTransazione(s.presentaEmail(),t);
            }

        } catch (Exception e) {
            throw new DAOException("Errore durante il recupero dello stock fittizio: " + e.getMessage(), e);
        }

        return wallet;
    }


    @Override
    public void salvaPortafoglio(VirtualWallet wallet) throws DAOException {
        if (wallet == null) throw new DAOException("Portafoglio nullo");
        addToCache(wallet);
    }

    @Override
    protected void doDeletePortafoglio(String email) throws DAOException {
        // Nella demo non c'è un DB fisico: la cache è già stata pulita da rimuoviPortafoglio.
        // Se hai un fintoDatabase, rimuovi qui l'entry.
    }



}