package org.project.ing.persistenza;

import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.dao.transazioni.TransactionDAO;
import org.project.dao.posizioni.WalletPositionDAO;
import org.project.ing.enumerations.PersistenzaSupportata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class DAOFactory {

    private static DAOFactory me = null;

    protected DAOFactory() {}

    public static synchronized DAOFactory getDAOFactory() {
        if (me == null) {
            try (InputStream in = DAOFactory.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (in == null) throw new IOException("config.properties non trovato nel classpath");
                Properties prop = new Properties();
                prop.load(in);
                String daoType = prop.getProperty("persistence.type").toUpperCase();
                PersistenzaSupportata version = PersistenzaSupportata.valueOf(daoType);
                me = switch (version) {
                    case FILESYSTEM -> new FileDAOFactory();
                    case DATABASE   -> new DBDAOFactory();
                    case DEMO       -> new DemoDAOFactory();
                };
            } catch (IllegalArgumentException | IOException e) {
                me = new DemoDAOFactory();
            }
        }
        return me;
    }

    public abstract StudenteDAO createStudenteDAO();
    public abstract SchoolClassDAO createSchoolClassDAO();
    public abstract ProfessoreDAO createProfessoreDAO();
    public abstract PortafoglioDAO createPortafoglioDAO();
    public abstract TransactionDAO createTransactionDAO();
    public abstract WalletPositionDAO createWalletPositionDAO();
}