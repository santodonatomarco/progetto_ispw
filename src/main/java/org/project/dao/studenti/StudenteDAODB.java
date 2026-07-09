package org.project.dao.studenti;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.ing.persistenza.DBConnection;
import org.project.model.*;
import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.professori.ProfessoreDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudenteDAODB extends StudenteDAO {

    private final SchoolClassDAO schoolClassDAO;
    private final ProfessoreDAO professoreDAO;

    public StudenteDAODB(SchoolClassDAO schoolClassDAO, ProfessoreDAO professoreDAO) {
        this.schoolClassDAO = schoolClassDAO;
        this.professoreDAO = professoreDAO;
    }

    @Override
    protected Studente doRetrieveStudenteByEmail(String email) throws DAOException {
        if (email == null || email.trim().isEmpty()) throw new DAOException("Email non valida");

        String sql = "SELECT email, nome, cognome, password_hash, auth_provider, classe, professore_email " +
                "FROM studente WHERE email = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return mapStudente(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("Errore SQL nel recupero dello studente: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected List<Studente> doRetrieveStudentiClasse(String nomeClasse) throws DAOException {
        if (nomeClasse == null || nomeClasse.trim().isEmpty()) throw new DAOException("Nome classe non valido");

        List<Studente> lista = new ArrayList<>();
        String sql = "SELECT email, nome, cognome, password_hash, auth_provider, classe, professore_email " +
                "FROM studente WHERE classe = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeClasse);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapStudente(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore recupero studenti della classe " + nomeClasse + ": " + e.getMessage());
        }
        return lista;
    }

    @Override
    protected void doSaveStudente(Studente studente) throws DAOException {
        String sql = "INSERT INTO studente (email, nome, cognome, password_hash, auth_provider, classe, professore_email) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE" +
                "nome = VALUES(nome), cognome = VALUES(cognome), " +
                "password_hash = VALUES(password_hash), auth_provider = VALUES(auth_provider), " +
                "classe = VALUES(classe), professore_email = VALUES(professore_email)";

        String nomeClasse = studente.classeFrequentata() != null ? studente.classeFrequentata().nome() : null;
        String emailProf  = (studente.classeFrequentata() != null && studente.classeFrequentata().teacher() != null)
                ? studente.classeFrequentata().teacher().presentaEmail() : null;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, studente.presentaEmail());
            st.setString(2, studente.presentaNome());
            st.setString(3, studente.presentaCognome());
            st.setString(4, studente.getPasswordHash());
            st.setString(5, studente.comeAccede().toString());
            st.setString(6, nomeClasse);
            st.setString(7, emailProf);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio studente su DB: " + e.getMessage());
        }
    }


    @Override
    protected void doDeleteStudente(String email) throws DAOException {
        String sql = "DELETE FROM studente WHERE email = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, email);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore eliminazione studente " + email + ": " + e.getMessage());
        }
    }

    private Studente mapStudente(ResultSet rs) throws SQLException, DAOException {
        String email        = rs.getString("email");
        String nome         = rs.getString("nome");
        String cognome      = rs.getString("cognome");
        String passwordHash = rs.getString("password_hash");
        String authProvider = rs.getString("auth_provider");
        String nomeClasse   = rs.getString("classe");
        String profEmail    = rs.getString("professore_email");

        AuthProvider provider = AuthProvider.valueOf(authProvider);
        Studente studente = new Studente(email, nome, cognome, provider);
        if (provider == AuthProvider.LOCAL && passwordHash != null && !passwordHash.isEmpty()) {
            studente.impostaPasswordHash(passwordHash);
        }

        if (nomeClasse != null && !nomeClasse.trim().isEmpty() &&
                profEmail != null && !profEmail.trim().isEmpty()) {
            try {
                Professore professore = professoreDAO.getProfessoreByEmail(profEmail);
                if (professore != null) {
                    SchoolClass classe = schoolClassDAO.getClasseByNomeEProfessore(nomeClasse, professore);
                    if (classe != null) studente.iscriviClasse(classe);
                }
            } catch (DAOException e) {
                // Classe non caricabile — lo studente rimane senza classe
            }
        }
        return studente;
    }
}
