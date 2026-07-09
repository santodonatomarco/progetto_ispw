package org.project.dao.wallets;

import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.DAOException;
import org.project.ing.enumerations.TipoTransazione;
import org.project.ing.factory.StockFactory;
import org.project.ing.persistenza.DBConnection;
import org.project.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;


public class PortafoglioDAODB extends PortafoglioDAO {

    private final StudenteDAO studenteDAO;
    private final StockFactory stockFactory;

    public PortafoglioDAODB(StudenteDAO studenteDAO, StockFactory stockFactory) {
        this.studenteDAO = studenteDAO;
        this.stockFactory = stockFactory;
    }

    @Override
    protected VirtualWallet doRetrievePortafoglioByEmail(String mail) throws DAOException {
        Studente studente = studenteDAO.getStudenteByEmail(mail);
        if (studente == null) return null;

        String sqlSaldo       = "SELECT saldo_disponibile FROM virtual_wallet WHERE studente_email = ?";
        String sqlPosizioni   = "SELECT simbolo, quantita, prezzo_medio_acquisto FROM wallet_position WHERE email_studente = ?";
        String sqlTransazioni = "SELECT simbolo, tipo, quantita, prezzo_al_momento, timestamp FROM transazione WHERE email_studente = ?";

        try (Connection conn = DBConnection.getInstance().getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(sqlSaldo)) {
                ps.setString(1, mail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studente.creaWallet(rs.getDouble("saldo_disponibile")); // nasce dentro Studente
                    } else {
                        return null;
                    }
                }
            }

            VirtualWallet wallet = studente.portafoglio(); // solo navigazione, non creazione

            try (PreparedStatement ps = conn.prepareStatement(sqlPosizioni)) {
                ps.setString(1, mail);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Stock stock = stockFactory.creaStock(rs.getString("simbolo"));
                        wallet.caricaPosizione(stock,
                                rs.getDouble("quantita"),
                                rs.getDouble("prezzo_medio_acquisto"));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlTransazioni)) {
                ps.setString(1, mail);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Stock stock = stockFactory.creaStock(rs.getString("simbolo"));
                        LocalDateTime ts = rs.getTimestamp("timestamp").toLocalDateTime();
                        wallet.caricaTransazione(stock,
                                TipoTransazione.valueOf(rs.getString("tipo")),
                                rs.getDouble("quantita"),
                                rs.getDouble("prezzo_al_momento"),
                                ts,
                                true); // dal DB sono sempre DONE
                    }
                }
            }

            return wallet;

        } catch (SQLException e) {
            throw new DAOException("Errore DB caricamento portafoglio: " + e.getMessage());
        } catch (Exception e) {
            throw new DAOException("Errore caricamento stock per il portafoglio: " + e.getMessage());
        }
    }

    @Override
    public void salvaPortafoglio(VirtualWallet wallet) throws DAOException {
        String email = wallet.proprietario().presentaEmail();
        String sql = "INSERT INTO virtual_wallet (studente_email, saldo_disponibile) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE saldo_disponibile = VALUES(saldo_disponibile)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setDouble(2, wallet.saldoDisponibile());
            ps.executeUpdate();
            addToCache(wallet);
        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio portafoglio: " + e.getMessage());
        }
    }


    @Override
    protected void doDeletePortafoglio(String email) throws DAOException {
        String sql = "DELETE FROM virtual_wallet WHERE studente_email = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, email);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore eliminazione portafoglio per " + email + ": " + e.getMessage());
        }
    }
}
