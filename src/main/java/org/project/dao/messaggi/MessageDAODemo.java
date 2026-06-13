package org.project.dao.messaggi;

import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.DAOException;
import org.project.model.Message;
import org.project.model.Professore;
import org.project.model.Studente;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAODemo extends MessageDAO {

    private List<Message> fintoDatabase;
    private StudenteDAO studenteDAO;
    private ProfessoreDAO professoreDAO;

    public MessageDAODemo(StudenteDAO studenteDAO, ProfessoreDAO professoreDAO) {
        super();
        this.fintoDatabase = new ArrayList<>();
        this.studenteDAO = studenteDAO;
        this.professoreDAO = professoreDAO;
        try {
            this.popolaDBFittizio();
        } catch (DAOException e) {
            System.err.println("[MessageDAODemo] Avviso: impossibile popolare il DB fittizio: " + e.getMessage());
        }
    }

    private void popolaDBFittizio() throws DAOException {
        // Recupero gli utenti demo dai rispettivi DAO
        Studente alice = studenteDAO.getStudenteByEmail("alice.verdi@student.it");
        Studente bob = studenteDAO.getStudenteByEmail("bob.neri@student.it");
        Studente carlo = studenteDAO.getStudenteByEmail("carlo.smith@student.it");
        Studente diana = studenteDAO.getStudenteByEmail("diana.jones@student.it");

        Professore mario = professoreDAO.getProfessoreByEmail("mario.rossi@univ.it");
        Professore lucia = professoreDAO.getProfessoreByEmail("lucia.bianchi@univ.it");

        if (alice == null || bob == null || carlo == null || diana == null || mario == null || lucia == null) {
            throw new DAOException("Utenti demo non trovati — verifica StudenteDAODemo e ProfessoreDAODemo");
        }

        // i messaggi vengono scambiati solamente tra studente e professore

        Message m1 = new Message(alice, mario, "Buongiorno Professor Rossi, ho una domanda sui compiti assegnati.", LocalDateTime.now().minusDays(2));
        Message m2 = new Message(mario, alice, "Ciao Alice, dimmi pure! Sono sempre disponibile per aiutare.", LocalDateTime.now().minusDays(2).plusHours(3));

        Message m3 = new Message(bob, lucia, "Prof. Bianchi, quando è la prossima verifica?", LocalDateTime.now().minusDays(1));
        Message m4 = new Message(lucia, bob, "Ciao Bob, la verifica è prevista per il prossimo lunedì.", LocalDateTime.now().minusDays(1).plusHours(1));


        fintoDatabase.add(m1);
        fintoDatabase.add(m2);
        fintoDatabase.add(m3);
        fintoDatabase.add(m4);
    }

    @Override
    protected List<Message> doRetrieveMessaggiRicevuti(String emailDestinatario) {
        List<Message> inbox = new ArrayList<>();
        for (Message m : fintoDatabase) {
            if (m.getDestinatario().presentaEmail().equals(emailDestinatario)) {
                inbox.add(m);
            }
        }
        return inbox;
    }

    @Override
    protected void doSaveMessaggio(Message messaggio) {
        fintoDatabase.add(messaggio);
    }
}