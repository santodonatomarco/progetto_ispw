package org.project.ing.persistenza;

import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.classi.SchoolClassDAODemo;
import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.professori.ProfessoreDAODemo;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.studenti.StudenteDAODemo;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.dao.wallets.PortafoglioDAODemo;
import org.project.ing.factory.StockFactory;
import org.project.ing.factory.StockFactoryDemo;

public class DemoDAOFactory extends DAOFactory {

    private ProfessoreDAO professoreDAOInstance;
    private SchoolClassDAO schoolClassDAOInstance;
    private StudenteDAO studenteDAOInstance;
    private PortafoglioDAO portafoglioDAOInstance;
    private StockFactory stockFactoryInstance;

    @Override
    public ProfessoreDAO createProfessoreDAO() {
        if (professoreDAOInstance == null) {
            professoreDAOInstance = new ProfessoreDAODemo();
        }
        return professoreDAOInstance;
    }

    @Override
    public SchoolClassDAO createSchoolClassDAO() {
        if (schoolClassDAOInstance == null) {
            // Inietto il ProfessoreDAO necessario per costruire la SchoolClass
            schoolClassDAOInstance = new SchoolClassDAODemo(createProfessoreDAO());
        }
        return schoolClassDAOInstance;
    }

    @Override
    public StudenteDAO createStudenteDAO() {
        if (studenteDAOInstance == null) {
            // Inietto lo SchoolClassDAO E ProfessoreDAO necessari per assegnare la classe allo studente
            studenteDAOInstance = new StudenteDAODemo(createSchoolClassDAO(), createProfessoreDAO());
        }
        return studenteDAOInstance;
    }

    @Override
    public PortafoglioDAO createPortafoglioDAO() {
        if (portafoglioDAOInstance == null) {
            // Inietto StudenteDAO e StockFactory necessari per il portafoglio
            portafoglioDAOInstance = new PortafoglioDAODemo(createStudenteDAO(), createStockFactory());
        }
        return portafoglioDAOInstance;
    }

    private StockFactory createStockFactory() {
        if (stockFactoryInstance == null) {
            stockFactoryInstance = new StockFactoryDemo();
        }
        return stockFactoryInstance;
    }
}