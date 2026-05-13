package org.project.dao.studenti;

import org.project.exceptions.DAOException;
import org.project.ing.enumerations.AuthProvider;
import org.project.ing.persistenza.DBConnection;
import org.project.model.Studente;
import org.project.model.StudenteLocale;
import org.project.model.StudenteOAuth;
import org.project.model.SchoolClass;
import org.project.dao.classi.SchoolClassDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class StudenteDAODB extends StudenteDAO {
    // SchoolClassDAO: garantisce un singleton per classe (una sola istanza per classe)
    private SchoolClassDAO schoolClassDAO;

    public StudenteDAODB(SchoolClassDAO schoolClassDAO) {
        this.schoolClassDAO = schoolClassDAO;
    }


    @Override
    protected Studente doRetrieveStudenteByEmail(String email) throws DAOException {
        if (email == null || email.trim().isEmpty()) {
            throw new DAOException("Email non valida");
        }

        String sql = "SELECT email, nome, cognome, password_hash, auth_provider, classe FROM studente WHERE email = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, email);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");
                    String passwordHash = rs.getString("password_hash");
                    String authProvider = rs.getString("auth_provider");
                    String nomeClasse = rs.getString("classe");

                    // Costruisci lo Studente corretto in base all'AuthProvider
                    Studente studente;
                    if (AuthProvider.LOCAL.toString().equals(authProvider)) {
                        studente = new StudenteLocale(email, nome, cognome);
                        ((StudenteLocale) studente).inserisciHashPassword(passwordHash != null ? passwordHash : "");
                    } else {
                        // OAuth (GOOGLE, MICROSOFT)
                        AuthProvider provider = AuthProvider.valueOf(authProvider);
                        studente = new StudenteOAuth(email, nome, cognome, provider);
                    }

                    // Assegna la classe di appartenenza se disponibile
                    // NOTA: Carichiamo la classe dal SchoolClassDAO che garantisce un singleton
                    if (nomeClasse != null && !nomeClasse.trim().isEmpty()) {
                        try {
                            SchoolClass classe = schoolClassDAO.getClasseByNome(nomeClasse);
                            studente.iscriviClasse(classe);
                        } catch (DAOException e) {
                            // Se la classe non può essere caricata, continua comunque
                            // Lo studente rimarrà senza classe assegnata
                        }
                    }

                    return studente;
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore SQL nel recupero dello studente: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new DAOException("AuthProvider non valido: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected List<Studente> doRetrieveStudentiClasse(String nomeClasse) throws DAOException {
        if (nomeClasse == null || nomeClasse.trim().isEmpty()) {
            throw new DAOException("Nome classe non valido");
        }

        List<Studente> lista = new ArrayList<>();
        String sql = "SELECT email, nome, cognome, password_hash, auth_provider FROM studente WHERE classe = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nomeClasse);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString("email");
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");
                    String passwordHash = rs.getString("password_hash");
                    String authProvider = rs.getString("auth_provider");

                    // Costruisci lo Studente corretto in base all'AuthProvider
                    Studente studente;
                    if (AuthProvider.LOCAL.toString().equals(authProvider)) {
                        studente = new StudenteLocale(email, nome, cognome);
                        ((StudenteLocale) studente).inserisciHashPassword(passwordHash != null ? passwordHash : "");
                    } else {
                        // OAuth (GOOGLE, MICROSOFT)
                        AuthProvider provider = AuthProvider.valueOf(authProvider);
                        studente = new StudenteOAuth(email, nome, cognome, provider);
                    }

                    lista.add(studente);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore recupero studenti della classe " + nomeClasse + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new DAOException("AuthProvider non valido: " + e.getMessage());
        }
        return lista;
    }

}
