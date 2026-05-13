package org.project.ing.persistenza;

import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.classi.SchoolClassDAOFile;
import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.professori.ProfessoreDAOFile;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.studenti.StudenteDAOFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileDAOFactory extends DAOFactory {

    private ProfessoreDAO professoreDAOInstance;
    private SchoolClassDAO schoolClassDAOInstance;
    private StudenteDAO studenteDAOInstance;

    // Variabili per memorizzare i percorsi letti dal config
    private String professoriFile;
    private String classiFile;
    private String studentiFile;

    public FileDAOFactory() {
        Properties prop = new Properties();

        // Carica il file di configurazione dal classpath
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                prop.load(in);
            } else {
                System.err.println("Attenzione: config.properties non trovato. Verranno usati i percorsi di default.");
            }
        } catch (IOException e) {
            System.err.println("Errore nella lettura di config.properties: " + e.getMessage());
        }

        // Legge le proprietà, usando il secondo parametro come fallback di sicurezza
        this.professoriFile = prop.getProperty("file.professori", "professori.csv");
        this.classiFile = prop.getProperty("file.classi", "classi.csv");
        this.studentiFile = prop.getProperty("file.studenti", "studenti.csv");
    }
    @Override
    public ProfessoreDAO createProfessoreDAO() {
        if (professoreDAOInstance == null) {
            professoreDAOInstance = new ProfessoreDAOFile(professoriFile);
        }
        return professoreDAOInstance;
    }

    @Override
    public SchoolClassDAO createSchoolClassDAO() {
        if (schoolClassDAOInstance == null) {
            schoolClassDAOInstance = new SchoolClassDAOFile(classiFile,createProfessoreDAO());
        }
        return schoolClassDAOInstance;
    }

    @Override
    public StudenteDAO createStudenteDAO() {
        if (studenteDAOInstance == null) {
            studenteDAOInstance = new StudenteDAOFile(studentiFile, createSchoolClassDAO());
        }
        return studenteDAOInstance;
    }
}