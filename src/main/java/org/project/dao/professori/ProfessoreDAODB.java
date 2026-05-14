package org.project.dao.professori;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.ing.persistenza.DBConnection;
import org.project.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfessoreDAODB extends ProfessoreDAO {

    public ProfessoreDAODB() {
        super();
    }

    @Override
    protected Professore doRetrieveProfessoreByEmail(String email) throws DAOException {
        if (email == null || email.trim().isEmpty()) {
            throw new DAOException("Email non valida");
        }

        String sql = "SELECT email, nome, cognome, password_hash, auth_provider FROM professore WHERE email = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, email);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapProfessore(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore SQL nel recupero del professore: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new DAOException("AuthProvider non valido: " + e.getMessage());
        }
        return null;
    }

    /**
     * Salva (o aggiorna) un professore nel DB usando UPSERT.
     * Supporta sia registrazione locale che OAuth.
     */
    @Override
    protected void doSaveProfessore(Professore professore) throws DAOException {
        String sql = "INSERT INTO professore (email, nome, cognome, password_hash, auth_provider) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (email) DO UPDATE SET " +
                "nome = EXCLUDED.nome, cognome = EXCLUDED.cognome, " +
                "password_hash = EXCLUDED.password_hash, auth_provider = EXCLUDED.auth_provider";

        String passwordHash = "";
        if (professore instanceof AutenticazioneLocale) {
            passwordHash = ((AutenticazioneLocale) professore).passwordHash();
        }

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, professore.presentaEmail());
            st.setString(2, professore.presentaNome());
            st.setString(3, professore.presentaCognome());
            st.setString(4, passwordHash);
            st.setString(5, professore.comeAccede().toString());

            st.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio professore su DB: " + e.getMessage());
        }
    }

    // ── Metodo privato di mapping ─────────────────────────────────────────────

    /**
     * Mappa un ResultSet a un oggetto Professore.
     * Evita la duplicazione di codice tra retrieve e salvataggio.
     */
    private Professore mapProfessore(ResultSet rs) throws SQLException {
        String email        = rs.getString("email");
        String nome         = rs.getString("nome");
        String cognome      = rs.getString("cognome");
        String passwordHash = rs.getString("password_hash");
        String authProvider = rs.getString("auth_provider");

        Professore professore;
        if (AuthProvider.LOCAL.toString().equals(authProvider)) {
            professore = new ProfessoreLocale(email, nome, cognome);
            ((ProfessoreLocale) professore).inserisciHashPassword(passwordHash != null ? passwordHash : "");
        } else {
            professore = new ProfessoreOAuth(email, nome, cognome, AuthProvider.valueOf(authProvider));
        }

        return professore;
    }
}