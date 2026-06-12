package org.project.dao.messaggi;

import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.DAOException;
import org.project.ing.persistenza.DBConnection;
import org.project.model.Message;
import org.project.model.Utente;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAODB extends MessageDAO {

    private StudenteDAO studenteDAO;
    private ProfessoreDAO professoreDAO;

    public MessageDAODB(StudenteDAO studenteDAO, ProfessoreDAO professoreDAO) {
        super();
        this.studenteDAO = studenteDAO;
        this.professoreDAO = professoreDAO;
    }

    @Override
    protected List<Message> doRetrieveMessaggiRicevuti(String emailDestinatario) throws DAOException {
        String sql = "SELECT mittente_email, destinatario_email, testo, data_invio " +
                "FROM messaggio WHERE destinatario_email = ? ORDER BY data_invio DESC";

        // ── Passo 1: leggi dati grezzi senza aprire altre query ──────────────────
        record RigaMessaggio(String mittente, String destinatario, String testo, LocalDateTime ts) {}
        List<RigaMessaggio> righe = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, emailDestinatario);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    righe.add(new RigaMessaggio(
                            rs.getString("mittente_email"),
                            rs.getString("destinatario_email"),
                            rs.getString("testo"),
                            rs.getTimestamp("data_invio").toLocalDateTime()
                    ));
                }
            } // ← ResultSet chiuso qui

        } catch (SQLException e) {
            throw new DAOException("Errore SQL nel recupero dei messaggi: " + e.getMessage());
        }

        // ── Passo 2: ora risolvi mittente/destinatario senza ResultSet aperto ─────
        List<Message> inbox = new ArrayList<>();
        for (RigaMessaggio r : righe) {
            Utente mittente     = trovaUtente(r.mittente());
            Utente destinatario = trovaUtente(r.destinatario());
            if (mittente != null && destinatario != null) {
                inbox.add(new Message(mittente, destinatario, r.testo(), r.ts()));
            }
        }
        return inbox;
    }
    @Override
    protected void doSaveMessaggio(Message messaggio) throws DAOException {
        String sql = "INSERT INTO messaggio (mittente_email, destinatario_email, testo, data_invio) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, messaggio.getMittente().presentaEmail());
            st.setString(2, messaggio.getDestinatario().presentaEmail());
            st.setString(3, messaggio.getTesto());
            st.setTimestamp(4, Timestamp.valueOf(messaggio.getTimestamp()));

            st.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio messaggio su DB: " + e.getMessage());
        }
    }

    private Message mapMessage(ResultSet rs) throws SQLException, DAOException {
        String mittenteEmail = rs.getString("mittente_email");
        String destinatarioEmail = rs.getString("destinatario_email");
        String testo = rs.getString("testo");
        LocalDateTime timestamp = rs.getTimestamp("data_invio").toLocalDateTime();

        Utente mittente = trovaUtente(mittenteEmail);
        Utente destinatario = trovaUtente(destinatarioEmail);

        if (mittente == null || destinatario == null) return null;

        Message m = new Message(mittente, destinatario, testo, timestamp);
        return m;
    }

    private Utente trovaUtente(String email) throws DAOException {
        Utente u = studenteDAO.getStudenteByEmail(email);
        if (u != null) return u;
        return professoreDAO.getProfessoreByEmail(email);
    }
}