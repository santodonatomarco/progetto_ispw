package org.project.ing.persistenza;

import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.classi.SchoolClassDAODB;
import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.professori.ProfessoreDAODB;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.studenti.StudenteDAODB;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.dao.wallets.PortafoglioDAODB;
import org.project.dao.transazioni.TransactionDAO;
import org.project.dao.transazioni.TransactionDAODB;
import org.project.dao.posizioni.WalletPositionDAO;
import org.project.dao.posizioni.WalletPositionDAODB;
import org.project.ing.factory.StockFactory;
import org.project.ing.factory.StockFactoryAPI;

public class DBDAOFactory extends DAOFactory {

    private ProfessoreDAO professoreDAOInstance;
    private SchoolClassDAO schoolClassDAOInstance;
    private StudenteDAO studenteDAOInstance;
    private PortafoglioDAO portafoglioDAOInstance;
    private TransactionDAO transactionDAOInstance;
    private WalletPositionDAO walletPositionDAOInstance;
    private StockFactory stockFactoryInstance;

    @Override
    public ProfessoreDAO createProfessoreDAO() {
        if (professoreDAOInstance == null)
            professoreDAOInstance = new ProfessoreDAODB();
        return professoreDAOInstance;
    }

    @Override
    public SchoolClassDAO createSchoolClassDAO() {
        if (schoolClassDAOInstance == null)
            schoolClassDAOInstance = new SchoolClassDAODB(createProfessoreDAO());
        return schoolClassDAOInstance;
    }

    @Override
    public StudenteDAO createStudenteDAO() {
        if (studenteDAOInstance == null)
            studenteDAOInstance = new StudenteDAODB(createSchoolClassDAO(), createProfessoreDAO());
        return studenteDAOInstance;
    }

    @Override
    public PortafoglioDAO createPortafoglioDAO() {
        if (portafoglioDAOInstance == null)
            portafoglioDAOInstance = new PortafoglioDAODB(createStudenteDAO(), createStockFactory());
        return portafoglioDAOInstance;
    }

    @Override
    public TransactionDAO createTransactionDAO() {
        if (transactionDAOInstance == null)
            transactionDAOInstance = new TransactionDAODB();
        return transactionDAOInstance;
    }

    @Override
    public WalletPositionDAO createWalletPositionDAO() {
        if (walletPositionDAOInstance == null)
            walletPositionDAOInstance = new WalletPositionDAODB();
        return walletPositionDAOInstance;
    }

    private StockFactory createStockFactory() {
        if (stockFactoryInstance == null)
            stockFactoryInstance = new StockFactoryAPI();
        return stockFactoryInstance;
    }
}