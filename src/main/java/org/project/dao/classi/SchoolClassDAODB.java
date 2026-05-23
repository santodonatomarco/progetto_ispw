package org.project.dao.classi;

import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.DAOException;
import org.project.ing.persistenza.DBConnection;
import org.project.model.Professore;
import org.project.model.SchoolClass;
import org.project.model.Studente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SchoolClassDAODB extends SchoolClassDAO {

    private StudenteDAO studenteDAO;  // iniettato opzionalmente per caricare gli studenti

    public SchoolClassDAODB() {
        super();
    }

    /** Permette di iniettare lo StudenteDAO dopo la costruzione (evita ciclo di dipendenze). */
    public void setStudenteDAO(StudenteDAO studenteDAO) {
        this.studenteDAO = studenteDAO;
    }

    @Override
    protected SchoolClass doRetrieveClasseByNomeEProfessore(String nomeClasse, Professore professore) throws DAOException {
        if (nomeClasse == null || nomeClasse.trim().isEmpty()) {
            throw new DAOException("Nome classe non valido");
        }
        if (professore == null) {
            throw new DAOException("Professore non può essere nullo");
        }

        String sql = "SELECT nome, budget_iniziale FROM schoolclass WHERE nome = ? AND professore_email = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nomeClasse);
            ps.setString(2, professore.presentaEmail());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("nome");
                    double budget = rs.getDouble("budget_iniziale");
                    SchoolClass classe = new SchoolClass(nome, professore);
                    classe.impostaBudget(budget);
                    return classe;
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore SQL nel recupero della classe: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<SchoolClass> getClassiByProfessore(Professore professore) throws DAOException {
        if (professore == null) {
            throw new DAOException("Il professore non può essere nullo");
        }

        List<SchoolClass> classi = new ArrayList<>();
        String sql = "SELECT nome, budget_iniziale FROM schoolclass WHERE professore_email = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, professore.presentaEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nomeClasse = rs.getString("nome");
                    double budget = rs.getDouble("budget_iniziale");

                    // Creiamo la classe passando l'oggetto professore ricevuto
                    SchoolClass classe = new SchoolClass(nomeClasse, professore);
                    classe.impostaBudget(budget);

                    // Carica gli studenti iscritti a questa classe
                    if (studenteDAO != null) {
                        try {
                            List<Studente> studenti = studenteDAO.getStudentiClasse(classe);
                            for (Studente s : studenti) classe.iscriviStudente(s);
                        } catch (DAOException e) {
                            // Non bloccare se gli studenti non si caricano
                        }
                    }

                    classi.add(classe);
                    addToCache(classe);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore SQL nel recupero delle classi del prof: " + e.getMessage());
        }

        return classi;
    }




    @Override
    public void salvaClasse(SchoolClass classe) throws DAOException {
        if (classe == null) throw new DAOException("La classe non può essere nulla");

        String sql = "INSERT INTO schoolclass (nome, professore_email) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE professore_email = VALUES(professore_email)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, classe.nome());
            ps.setString(2, classe.teacher() != null ? classe.teacher().presentaEmail() : null);

            ps.executeUpdate();
            addToCache(classe);

        } catch (SQLException e) {
            throw new DAOException("Errore nel salvataggio della classe: " + e.getMessage());
        }
    }
}