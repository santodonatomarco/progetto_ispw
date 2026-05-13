package org.project.dao.classi;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.DBConnection;
import org.project.model.Professore;
import org.project.model.SchoolClass;
import org.project.dao.professori.ProfessoreDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SchoolClassDAODB extends SchoolClassDAO {

    private ProfessoreDAO professoreDAO;

    public SchoolClassDAODB(ProfessoreDAO professoreDAO) {
        super();
        this.professoreDAO = professoreDAO;
    }

    @Override
    protected SchoolClass doRetrieveClasseByNome(String nomeClasse) throws DAOException {
        String sql = "SELECT nome, professore_email FROM schoolclass WHERE nome = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nomeClasse);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("nome");
                    String professoreEmail = rs.getString("professore_email");

                    // Deleghiamo la creazione/recupero del professore
                    Professore professore = professoreDAO.getProfessoreByEmail(professoreEmail);
                    if (professore == null) {
                        throw new DAOException("Impossibile caricare il professore della classe " + nome);
                    }

                    return new SchoolClass(nome, professore);
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
        String sql = "SELECT nome FROM schoolclass WHERE professore_email = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, professore.presentaEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nomeClasse = rs.getString("nome");

                    // Creiamo la classe passando l'oggetto professore ricevuto
                    SchoolClass classe = new SchoolClass(nomeClasse, professore);
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

            // Sincronizziamo la Cache ereditata da CachedDAO
            addToCache(classe);

        } catch (SQLException e) {
            throw new DAOException("Errore nel salvataggio della classe: " + e.getMessage());
        }
    }
}