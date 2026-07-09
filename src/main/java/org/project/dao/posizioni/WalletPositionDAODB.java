package org.project.dao.posizioni;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.DBConnection;
import org.project.ing.service.StockService;
import org.project.model.Stock;
import org.project.model.WalletPosition;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WalletPositionDAODB extends WalletPositionDAO {

    // Tabella: wallet_position (email_studente, simbolo, quantita, prezzo_medio_acquisto)

    @Override
    protected void doSavePosizione(String email, WalletPosition p) throws DAOException {
        String sql = "INSERT INTO wallet_position (email_studente, simbolo, quantita, prezzo_medio_acquisto) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            // FIX: era st.setString(1, p.stock().simbolo()) — il simbolo al posto dell'email!
            st.setString(1, email);
            st.setString(2, p.stock().simbolo());
            st.setDouble(3, p.quantita());
            st.setDouble(4, p.prezzoMedioAcquisto());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio posizione: " + e.getMessage());
        }
    }

    @Override
    protected void doUpdatePosizione(String email, WalletPosition p) throws DAOException {
        String sql = "UPDATE wallet_position SET quantita = ?, prezzo_medio_acquisto = ? " +
                "WHERE email_studente = ? AND simbolo = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setDouble(1, p.quantita());
            st.setDouble(2, p.prezzoMedioAcquisto());
            st.setString(3, email);
            st.setString(4, p.stock().simbolo());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore aggiornamento posizione: " + e.getMessage());
        }
    }

    @Override
    protected void doDeletePosizione(String email, WalletPosition p) throws DAOException {
        String sql = "DELETE FROM wallet_position WHERE email_studente = ? AND simbolo = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, email);
            st.setString(2, p.stock().simbolo());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore eliminazione posizione: " + e.getMessage());
        }
    }

    @Override
    protected void doDeletePosizioniByEmail(String email) throws DAOException {
        String sql = "DELETE FROM wallet_position WHERE email_studente = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, email);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore eliminazione posizioni per " + email + ": " + e.getMessage());
        }
    }

    @Override
    protected List<WalletPosition> doRetrievePosizioniByEmail(String email) throws DAOException {
        String sql = "SELECT simbolo, quantita, prezzo_medio_acquisto " +
                "FROM wallet_position WHERE email_studente = ?";
        List<WalletPosition> lista = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String simbolo     = rs.getString("simbolo");
                    double quantita    = rs.getDouble("quantita");
                    double prezzoMedio = rs.getDouble("prezzo_medio_acquisto");
                    Stock stock = StockService.getInstance().ottieniOCreaStock(simbolo);
                    lista.add(new WalletPosition(stock, quantita, prezzoMedio));
                }
            }
        } catch (Exception e) {
            throw new DAOException("Errore lettura posizioni da DB: " + e.getMessage());
        }
        return lista;
    }
}
