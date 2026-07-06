package org.project.control;

import org.project.dao.messaggi.MessageDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.professori.ProfessoreDAO;
import org.project.exceptions.ControllerException;
import org.project.exceptions.DAOException;
import org.project.ing.persistenza.DAOFactory;
import org.project.model.*;
import org.project.view.bean.*;

import java.util.ArrayList;
import java.util.List;


public class ExchangeMessagesAppController {

    public MessageBean inviaMessaggio(SessioneBean sessione, InvioMessaggioBean input)
            throws ControllerException {

        Utente mittente = validaSessioneEOttieniUtente(sessione);

        if (mittente.presentaEmail().equalsIgnoreCase(input.getDestinatario().trim()))
            throw new ControllerException("Non puoi inviare un messaggio a te stesso.");

        DAOFactory factory = DAOFactory.getDAOFactory();
        MessageDAO messageDAO = factory.createMessageDAO();

        try {
            // Cerchiamo il destinatario interrogando i DAO specifici
            Utente destinatario = trovaUtentePerEmail(input.getDestinatario().trim().toLowerCase());

            if (destinatario == null)
                throw new ControllerException("Utente destinatario \"" + input.getDestinatario() + "\" non trovato.");

            Message nuovoMessaggio = new Message(mittente, destinatario, input.getTesto());

            destinatario.aggiungiAllaInbox(nuovoMessaggio);

            messageDAO.salvaMessaggio(nuovoMessaggio);

            return toMessageBean(nuovoMessaggio);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante l'invio del messaggio al server.", e);
        }
    }

    // ── Lettura Inbox ─────────────────────────────────────────────────────────

    /**
     * Recupera l'elenco di tutti i messaggi ricevuti dall'utente loggato.
     */
    public List<MessageBean> ottieniInbox(SessioneBean sessione) throws ControllerException {

        Utente utenteLoggato = validaSessioneEOttieniUtente(sessione);

        DAOFactory factory = DAOFactory.getDAOFactory();
        MessageDAO messageDAO = factory.createMessageDAO();

        try {
            List<Message> messaggi = messageDAO.getMessaggiRicevuti(utenteLoggato);

            List<MessageBean> beans = new ArrayList<>();
            if (messaggi != null) {
                for (Message m : messaggi) {
                    beans.add(toMessageBean(m));
                }
            }
            return beans;

        } catch (DAOException e) {
            throw new ControllerException("Errore nel recupero della casella di posta.", e);
        }
    }

    // ── Metodi privati ────────────────────────────────────────────────────────

    /**
     * Cerca un utente nel sistema interrogando prima gli studenti e poi i professori.
     * Restituisce l'istanza astratta Utente per sfruttare il polimorfismo.
     */
    private Utente trovaUtentePerEmail(String email) throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory();
        StudenteDAO studenteDAO = factory.createStudenteDAO();
        ProfessoreDAO professoreDAO = factory.createProfessoreDAO();

        Studente studente = studenteDAO.getStudenteByEmail(email);
        if (studente != null) return studente;

        Professore professore = professoreDAO.getProfessoreByEmail(email);
        if (professore != null) return professore;

        return null; // Nessun utente trovato con questa email
    }

    /**
     * Valida la sessione e restituisce l'utente corrente.
     */
    private Utente validaSessioneEOttieniUtente(SessioneBean sessione) throws ControllerException {
        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null)
            throw new ControllerException("Sessione non valida o scaduta.");

        Studente studente = sessioneModel.getStudenteCorrente();
        if (studente != null) return studente;

        Professore professore = sessioneModel.getProfessorCorrente();
        if (professore != null) return professore;

        throw new ControllerException("Nessun utente autenticato associato alla sessione.");
    }

    // ── Conversione model → bean ──────────────────────────────────────────────

    private MessageBean toMessageBean(Message m) {
        String nominativoMittente = m.getMittente().presentaNome() + " " + m.getMittente().presentaCognome();
        String nominativoDestinatario = m.getDestinatario().presentaNome() + " " + m.getDestinatario().presentaCognome();

        return new MessageBean(
                m.getMittente().presentaEmail(),
                nominativoMittente,
                m.getDestinatario().presentaEmail(),
                nominativoDestinatario,
                m.getTesto(),
                m.getTimestamp()
        );
    }
}




