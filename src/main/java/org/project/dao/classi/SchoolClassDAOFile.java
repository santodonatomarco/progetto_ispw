package org.project.dao.classi;

import org.project.exceptions.DAOException;
import org.project.model.SchoolClass;
import org.project.model.Professore;
import org.project.dao.professori.ProfessoreDAO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SchoolClassDAOFile extends SchoolClassDAO {

    private String fileName;
    private static final String CSV_SEPARATOR = ";";
    private ProfessoreDAO professoreDAO;

    public SchoolClassDAOFile(String fileName, ProfessoreDAO professoreDAO) {
        super();
        this.fileName = fileName;
        this.professoreDAO = professoreDAO;
    }

    @Override
    protected SchoolClass doRetrieveClasseByNome(String nomeCercato) throws DAOException {
        File file = new File(fileName);
        if (!file.exists()) throw new DAOException("File classi non trovato");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(CSV_SEPARATOR, -1);
                if (parts.length >= 2) {
                    String nome = parts[0].trim();
                    if (nome.equals(nomeCercato)) {
                        String emailProfessore = parts[1].trim();

                        // Ora il ProfessoreDAO esiste e lo usiamo!
                        Professore professore = professoreDAO.getProfessoreByEmail(emailProfessore);
                        if (professore == null) {
                            throw new DAOException("Professore inesistente per la classe: " + nome);
                        }
                        return new SchoolClass(nome, professore);
                    }
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file classi: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<SchoolClass> getClassiByProfessore(Professore professore) throws DAOException {
        if (professore == null) {
            throw new DAOException("Il professore non può essere nullo");
        }

        List<SchoolClass> classi = new ArrayList<>();
        File file = new File(fileName);

        if (!file.exists()) {
            throw new DAOException("File classi non trovato");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(CSV_SEPARATOR, -1);
                if (parts.length >= 2) {
                    String emailProfessore = parts[1].trim();

                    // Se l'email corrisponde a quella del professore passato
                    if (emailProfessore.equals(professore.presentaEmail())) {
                        String nomeClasse = parts[0].trim();

                        // Creiamo la classe usando direttamente l'oggetto professore
                        SchoolClass classe = new SchoolClass(nomeClasse, professore);
                        classi.add(classe);
                        addToCache(classe);
                    }
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file classi: " + e.getMessage());
        }

        return classi;
    }



    @Override
    public void salvaClasse(SchoolClass classe) throws DAOException {
        // Implementazione per salvare/aggiornare su file CSV...
        // Ricordati di chiamare addToCache(classe) alla fine del salvataggio!
    }
}