package org.project.ing.persistenza;

import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.classi.SchoolClassDAODemo;
import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.professori.ProfessoreDAODemo;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.studenti.StudenteDAODemo;

public class DemoDAOFactory extends DAOFactory {

    private ProfessoreDAO professoreDAOInstance;
    private SchoolClassDAO schoolClassDAOInstance;
    private StudenteDAO studenteDAOInstance;

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
            // Inietto lo SchoolClassDAO necessario per assegnare la classe allo studente
            studenteDAOInstance = new StudenteDAODemo(createSchoolClassDAO());
        }
        return studenteDAOInstance;
    }
}