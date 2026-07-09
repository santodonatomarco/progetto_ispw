package org.project.ing.persistenza;

import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.classi.SchoolClassDAOFile;
import org.project.dao.messaggi.MessageDAO;
import org.project.dao.messaggi.MessageDAOFile;
import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.professori.ProfessoreDAOFile;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.studenti.StudenteDAOFile;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.dao.wallets.PortafoglioDAOFile;
import org.project.dao.transazioni.TransactionDAO;
import org.project.dao.transazioni.TransactionDAOFile;
import org.project.dao.posizioni.WalletPositionDAO;
import org.project.dao.posizioni.WalletPositionDAOFile;
import org.project.ing.factory.StockFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileDAOFactory extends DAOFactory {

    private ProfessoreDAO professoreDAOInstance;
    private SchoolClassDAO schoolClassDAOInstance;
    private StudenteDAO studenteDAOInstance;
    private PortafoglioDAO portafoglioDAOInstance;
    private TransactionDAO transactionDAOInstance;
    private WalletPositionDAO walletPositionDAOInstance;
    private MessageDAO messageDAOInstance;

    private final String professoriFile;
    private final String classiFile;
    private final String studentiFile;
    private final String walletFile;
    private final String posizioniFile;
    private final String transazioniFile;
    private final String messaggiFile;

    public FileDAOFactory() {
        Properties prop = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) prop.load(in);
        } catch (IOException e) {
            System.err.println("Errore config.properties: " + e.getMessage());
        }
        this.professoriFile  = prop.getProperty("file.professori",  "professori.csv");
        this.classiFile      = prop.getProperty("file.classi",      "classi.csv");
        this.studentiFile    = prop.getProperty("file.studenti",    "studenti.csv");
        this.walletFile      = prop.getProperty("file.wallet",      "wallet.csv");
        this.posizioniFile   = prop.getProperty("file.posizioni",   "posizioni.csv");
        this.transazioniFile = prop.getProperty("file.transazioni", "transazioni.csv");
        this.messaggiFile = prop.getProperty("file.messaggi",    "messaggi.csv");
    }

    @Override public ProfessoreDAO createProfessoreDAO() {
        if (professoreDAOInstance == null)
            professoreDAOInstance = new ProfessoreDAOFile(professoriFile);
        return professoreDAOInstance;
    }

    @Override public SchoolClassDAO createSchoolClassDAO() {
        if (schoolClassDAOInstance == null) {
            SchoolClassDAOFile file = new SchoolClassDAOFile(classiFile);
            if (studenteDAOInstance != null) {
                file.setStudenteDAO(studenteDAOInstance);
            }
            schoolClassDAOInstance = file;
        }
        return schoolClassDAOInstance;
    }

    @Override public StudenteDAO createStudenteDAO() {
        if (studenteDAOInstance == null) {
            studenteDAOInstance = new StudenteDAOFile(studentiFile, createSchoolClassDAO(), createProfessoreDAO());
            if (schoolClassDAOInstance instanceof SchoolClassDAOFile scf)
                scf.setStudenteDAO(studenteDAOInstance);
            studenteDAOInstance.setPortafoglioDAO(createPortafoglioDAO());
        }
        return studenteDAOInstance;
    }

    @Override public PortafoglioDAO createPortafoglioDAO() {
        if (portafoglioDAOInstance == null) {
            portafoglioDAOInstance = new PortafoglioDAOFile(walletFile, posizioniFile, transazioniFile,
                    createStudenteDAO(), createStockFactory());
            portafoglioDAOInstance.setTransactionDAO(createTransactionDAO());
            portafoglioDAOInstance.setWalletPositionDAO(createWalletPositionDAO());
        }
        return portafoglioDAOInstance;
    }

    @Override public TransactionDAO createTransactionDAO() {
        if (transactionDAOInstance == null)
            transactionDAOInstance = new TransactionDAOFile(transazioniFile);
        return transactionDAOInstance;
    }

    @Override public WalletPositionDAO createWalletPositionDAO() {
        if (walletPositionDAOInstance == null)
            walletPositionDAOInstance = new WalletPositionDAOFile(posizioniFile);
        return walletPositionDAOInstance;
    }

    @Override public MessageDAO createMessageDAO() {
        if (messageDAOInstance == null)
            messageDAOInstance = new MessageDAOFile(messaggiFile, createStudenteDAO(), createProfessoreDAO());
        return messageDAOInstance;
    }


    private StockFactory createStockFactory() { return StockFactory.getInstance(); }
}
