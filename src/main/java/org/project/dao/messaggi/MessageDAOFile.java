package org.project.dao.messaggi;

import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.DAOException;
import org.project.model.Message;
import org.project.model.Utente;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// la struttura del file è: mittente_email;destinatario_email;testo;timestamp

public class MessageDAOFile extends MessageDAO {

    private String fileName;
    private static final String CSV_SEPARATOR = ";";
    private StudenteDAO studenteDAO;
    private ProfessoreDAO professoreDAO;

    public MessageDAOFile(String fileName, StudenteDAO studenteDAO, ProfessoreDAO professoreDAO) {
        super();
        this.fileName = fileName;
        this.studenteDAO = studenteDAO;
        this.professoreDAO = professoreDAO;
    }

    @Override
    protected List<Message> doRetrieveMessaggiRicevuti(String emailDestinatario) throws DAOException {
        List<Message> inbox = new ArrayList<>();
        File file = new File(fileName);

        if (!file.exists()) return inbox; // Nessun messaggio salvato finora

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(CSV_SEPARATOR, -1);

                // Indice 1 corrisponde a destinatario_email
                if (parts.length >= 4 && parts[1].trim().equals(emailDestinatario)) {
                    Message m = parseMessage(parts);
                    if (m != null) inbox.add(m);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore lettura file messaggi: " + e.getMessage());
        }
        return inbox;
    }

    @Override
    protected void doSaveMessaggio(Message messaggio) throws DAOException {
        File file = new File(fileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(toCSV(messaggio));
            bw.newLine();
        } catch (IOException e) {
            throw new DAOException("Errore scrittura file messaggi: " + e.getMessage());
        }
    }

    private String toCSV(Message m) {
        return String.join(CSV_SEPARATOR,
                m.getMittente().presentaEmail(),
                m.getDestinatario().presentaEmail(),
                m.getTesto().replace("\n", " "), // Evita che gli accapo rompano il CSV
                m.getTimestamp().toString()
        );
    }

    private Message parseMessage(String[] parts) throws DAOException {
        String mittenteEmail = parts[0].trim();
        String destinatarioEmail = parts[1].trim();
        String testo = parts[2].trim();
        LocalDateTime timestamp = LocalDateTime.parse(parts[3].trim());

        Utente mittente = trovaUtente(mittenteEmail);
        Utente destinatario = trovaUtente(destinatarioEmail);

        if (mittente == null || destinatario == null) return null;

        return new Message(mittente, destinatario, testo, timestamp);

    }

    private Utente trovaUtente(String email) throws DAOException {
        Utente u = studenteDAO.getStudenteByEmail(email);
        if (u != null) return u;
        return professoreDAO.getProfessoreByEmail(email);
    }
}