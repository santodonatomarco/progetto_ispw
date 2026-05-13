package org.project.dao.professori;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.ing.persistenza.DBConnection;
import org.project.model.Professore;
import org.project.model.ProfessoreLocale;
import org.project.model.ProfessoreOAuth;

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
        String sql = "SELECT email, nome, cognome, password_hash, auth_provider FROM professore WHERE email = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, email);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");
                    String passwordHash = rs.getString("password_hash");
                    String authProvider = rs.getString("auth_provider");

                    if (AuthProvider.LOCAL.toString().equals(authProvider)) {
                        ProfessoreLocale prof = new ProfessoreLocale(email, nome, cognome);
                        prof.inserisciHashPassword(passwordHash != null ? passwordHash : "");
                        return prof;
                    } else {
                        return new ProfessoreOAuth(email, nome, cognome, AuthProvider.valueOf(authProvider));
                    }
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DAOException("Errore recupero professore da DB: " + e.getMessage());
        }
        return null;
    }
}