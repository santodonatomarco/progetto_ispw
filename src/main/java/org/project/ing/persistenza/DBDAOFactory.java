package org.project.ing.persistenza;

import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.classi.SchoolClassDAODB;
import org.project.dao.messaggi.MessageDAO;
import org.project.dao.messaggi.MessageDAODB;
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

public class DBDAOFactory extends DAOFactory {

    private ProfessoreDAO professoreDAOInstance;
    private SchoolClassDAO schoolClassDAOInstance;
    private StudenteDAO studenteDAOInstance;
    private PortafoglioDAO portafoglioDAOInstance;
    private TransactionDAO transactionDAOInstance;
    private WalletPositionDAO walletPositionDAOInstance;
    private MessageDAODB messageDAODBInstance;

    @Override
    public ProfessoreDAO createProfessoreDAO() {
        if (professoreDAOInstance == null)
            professoreDAOInstance = new ProfessoreDAODB();
        return professoreDAOInstance;
    }

    @Override
    public SchoolClassDAO createSchoolClassDAO() {
        if (schoolClassDAOInstance == null) {
            SchoolClassDAODB db = new SchoolClassDAODB();
            if (studenteDAOInstance != null) {
                db.setStudenteDAO(studenteDAOInstance);  // ← inietta sul schoolClass, non sullo studente
            }
            schoolClassDAOInstance = db;
        }
        return schoolClassDAOInstance;
    }

    @Override
    public StudenteDAO createStudenteDAO() {
        if (studenteDAOInstance == null) {
            studenteDAOInstance = new StudenteDAODB(createSchoolClassDAO(), createProfessoreDAO());
            // Risolvi la dipendenza circolare SchoolClass ↔ Studente
            if (schoolClassDAOInstance instanceof SchoolClassDAODB scdb) {
                scdb.setStudenteDAO(studenteDAOInstance);
            }
            // Inietta PortafoglioDAO per la cascade delete.
            // createPortafoglioDAO() richiama createStudenteDAO() internamente,
            // ma a questo punto studenteDAOInstance è già settato → restituisce
            // l'istanza esistente senza ricorsione infinita.
            studenteDAOInstance.setPortafoglioDAO(createPortafoglioDAO());
        }
        return studenteDAOInstance;
    }

    @Override
    public PortafoglioDAO createPortafoglioDAO() {
        if (portafoglioDAOInstance == null) {
            portafoglioDAOInstance = new PortafoglioDAODB(createStudenteDAO(), createStockFactory());
            // Inietta TransactionDAO e WalletPositionDAO per la cascade delete
            portafoglioDAOInstance.setTransactionDAO(createTransactionDAO());
            portafoglioDAOInstance.setWalletPositionDAO(createWalletPositionDAO());
        }
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
        return StockFactory.getInstance();
    }


    @Override
    public MessageDAO createMessageDAO() {
        if (messageDAODBInstance == null)
            messageDAODBInstance = new MessageDAODB(createStudenteDAO(), createProfessoreDAO());
        return messageDAODBInstance;
    }
}
