package org.project.ing.persistenza;

import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.classi.SchoolClassDAODemo;
import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.professori.ProfessoreDAODemo;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.studenti.StudenteDAODemo;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.dao.wallets.PortafoglioDAODemo;
import org.project.dao.transazioni.TransactionDAO;
import org.project.dao.transazioni.TransactionDAODemo;
import org.project.dao.posizioni.WalletPositionDAO;
import org.project.dao.posizioni.WalletPositionDAODemo;
import org.project.ing.factory.StockFactory;
import org.project.ing.factory.StockFactoryDemo;

public class DemoDAOFactory extends DAOFactory {

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
            professoreDAOInstance = new ProfessoreDAODemo();
        return professoreDAOInstance;
    }

    @Override
    public SchoolClassDAO createSchoolClassDAO() {
        if (schoolClassDAOInstance == null)
            schoolClassDAOInstance = new SchoolClassDAODemo(createProfessoreDAO());
        return schoolClassDAOInstance;
    }

    @Override
    public StudenteDAO createStudenteDAO() {
        if (studenteDAOInstance == null)
            studenteDAOInstance = new StudenteDAODemo(createSchoolClassDAO(), createProfessoreDAO());
        return studenteDAOInstance;
    }

    @Override
    public PortafoglioDAO createPortafoglioDAO() {
        if (portafoglioDAOInstance == null)
            portafoglioDAOInstance = new PortafoglioDAODemo(createStudenteDAO(), createStockFactory());
        return portafoglioDAOInstance;
    }

    @Override
    public TransactionDAO createTransactionDAO() {
        if (transactionDAOInstance == null)
            transactionDAOInstance = new TransactionDAODemo();
        return transactionDAOInstance;
    }

    @Override
    public WalletPositionDAO createWalletPositionDAO() {
        if (walletPositionDAOInstance == null)
            walletPositionDAOInstance = new WalletPositionDAODemo();
        return walletPositionDAOInstance;
    }

    private StockFactory createStockFactory() {
        if (stockFactoryInstance == null)
            stockFactoryInstance = new StockFactoryDemo();
        return stockFactoryInstance;
    }
}