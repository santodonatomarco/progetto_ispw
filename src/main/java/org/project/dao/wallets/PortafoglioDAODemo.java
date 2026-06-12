package org.project.dao.wallets;

import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.DAOException;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.factory.StockFactory;
import org.project.ing.factory.WalletFactory;
import org.project.model.*;

public class PortafoglioDAODemo extends PortafoglioDAO {

    private final StudenteDAO studenteDAO;
    private final StockFactory stockFactory;

    public PortafoglioDAODemo(StudenteDAO studenteDAO, StockFactory stockFactory) {
        // Le dipendenze vengono iniettate dalla DAOFactory
        this.studenteDAO = studenteDAO;
        this.stockFactory = stockFactory;
    }

    @Override
    protected VirtualWallet doRetrievePortafoglioByEmail(String mail) throws DAOException {

        // 1. Recupero lo studente tramite il suo DAO (evita la circular dependency)
        Studente s = studenteDAO.getStudenteByEmail(mail);

        if (s == null) {
            return null; // Lo studente non esiste, quindi niente portafoglio
        }

        // 2. Uso WalletFactory per creare il portafoglio con il budget della classe
        VirtualWallet wallet = WalletFactory.creaWalletPerStudente(s);

        try {
            // 3. Uso la factory per recuperare lo stock
            // Lancia un'eccezione generica che dobbiamo gestire
            Stock apple = stockFactory.creaStock("AAPL");

            // 4. Aggiungo una posizione fittizia iniziale
            WalletPosition p = new WalletPosition(apple, 10, 180.0);
            wallet.aggiungiPosizione(p);

            // 5. Aggiungo la transazione corrispondente
            Transaction t = new Transaction(
                    apple,
                    TipoTransazione.BUY,
                    10,
                    180.0
            );
            t.completaTransazione(); // Segna la transazione come DONE
            wallet.aggiungiTransazione(t);

        } catch (Exception e) {
            // Gestiamo l'eccezione della Factory incapsulandola in una DAOException
            throw new DAOException("Errore durante il recupero dello stock fittizio: " + e.getMessage(), e);
        }

        return wallet;
    }

    // NOTA: Se implementerai un metodo salvaPortafoglio(),
    // nella versione Demo ti basterà aggiornare la Cache ereditata!

    @Override
    public void salvaPortafoglio(VirtualWallet wallet) throws DAOException {
        if (wallet == null) throw new DAOException("Portafoglio nullo");
        // Nella demo non c'è un DB fisico, ci basta aggiornare la cache ereditata da CachedDAO
        addToCache(wallet);
    }

    @Override
    protected void doDeletePortafoglio(String email) throws DAOException {
        // Nella demo non c'è un DB fisico: la cache è già stata pulita da rimuoviPortafoglio.
        // Se hai un fintoDatabase, rimuovi qui l'entry.
    }



}