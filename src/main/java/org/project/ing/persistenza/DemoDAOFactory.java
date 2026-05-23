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

public class DemoDAOFactory extends DAOFactory {

    private ProfessoreDAO professoreDAOInstance;
    private SchoolClassDAO schoolClassDAOInstance;
    private StudenteDAO studenteDAOInstance;
    private PortafoglioDAO portafoglioDAOInstance;
    private TransactionDAO transactionDAOInstance;
    private WalletPositionDAO walletPositionDAOInstance;

    @Override
    public ProfessoreDAO createProfessoreDAO() {
        if (professoreDAOInstance == null)
            professoreDAOInstance = new ProfessoreDAODemo();
        return professoreDAOInstance;
    }

    @Override
    public SchoolClassDAO createSchoolClassDAO() {
        if (schoolClassDAOInstance == null) {
            schoolClassDAOInstance = new SchoolClassDAODemo(createProfessoreDAO());
            // Inietta StudenteDAO se già creato (per risolvere dipendenza circolare)
            if (studenteDAOInstance != null && studenteDAOInstance instanceof StudenteDAODemo) {
                ((SchoolClassDAODemo) schoolClassDAOInstance).setStudenteDAO(studenteDAOInstance);
            }
        }
        return schoolClassDAOInstance;
    }

    @Override
    public StudenteDAO createStudenteDAO() {
        if (studenteDAOInstance == null) {
            studenteDAOInstance = new StudenteDAODemo(createSchoolClassDAO(), createProfessoreDAO());
            // Inietta StudenteDAO nel SchoolClassDAO per caricare gli studenti
            if (schoolClassDAOInstance != null && schoolClassDAOInstance instanceof SchoolClassDAODemo) {
                ((SchoolClassDAODemo) schoolClassDAOInstance).setStudenteDAO(studenteDAOInstance);
            }
        }
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


        return StockFactory.getInstance();
    }
}