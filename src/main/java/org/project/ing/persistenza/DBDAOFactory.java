package org.project.ing.persistenza;

import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.classi.SchoolClassDAODB;
import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.professori.ProfessoreDAODB;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.studenti.StudenteDAODB;

public class DBDAOFactory extends DAOFactory {

    private ProfessoreDAO professoreDAOInstance;
    private SchoolClassDAO schoolClassDAOInstance;
    private StudenteDAO studenteDAOInstance;

    @Override
    public ProfessoreDAO createProfessoreDAO() {
        if (professoreDAOInstance == null) {
            professoreDAOInstance = new ProfessoreDAODB();
        }
        return professoreDAOInstance;
    }

    @Override
    public SchoolClassDAO createSchoolClassDAO() {
        if (schoolClassDAOInstance == null) {
            schoolClassDAOInstance = new SchoolClassDAODB(createProfessoreDAO());
        }
        return schoolClassDAOInstance;
    }

    @Override
    public StudenteDAO createStudenteDAO() {
        if (studenteDAOInstance == null) {
            studenteDAOInstance = new StudenteDAODB(createSchoolClassDAO());
        }
        return studenteDAOInstance;
    }
}