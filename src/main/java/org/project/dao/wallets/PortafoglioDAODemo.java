package org.project.dao.wallets;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.factory.StockFactory;
import org.project.model.*;
import org.project.dao.studenti.StudenteDAO;

public class PortafoglioDAODemo extends PortafoglioDAO {

    private final StudenteDAO studenteDAO;
    private final StockFactory stockFactory;

    public PortafoglioDAODemo(
            StudenteDAO studenteDAO, StockFactory stockFactory
    ) {
        this.studenteDAO = studenteDAO;
        this.stockFactory = stockFactory;
    }

    @Override
    protected VirtualWallet doRetrievePortafoglioByEmail(String mail) throws DAOException {

        Studente s = studenteDAO.getStudenteByEmail(mail);

        if (s == null) {
            return null;
        }

        VirtualWallet wallet = new VirtualWallet(s, 5000);

        Stock apple = stockFactory.creaStock("AAPL");

        WalletPosition p = new WalletPosition(apple, 10, 180);
        wallet.aggiungiPosizione(p);

        Transaction t = new Transaction(
                apple,
                TipoTransazione.BUY,
                10,
                180
        );

        t.completaTransazione();

        wallet.aggiungiTransazione(t);

        return wallet;
    }
}