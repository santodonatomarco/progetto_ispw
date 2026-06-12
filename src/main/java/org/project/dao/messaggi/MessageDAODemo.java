package org.project.dao.messaggi;

import org.project.exceptions.DAOException;
import org.project.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageDAODemo extends MessageDAO {

    private List<Message> fintoDatabase;

    public MessageDAODemo() {
        super();
        this.fintoDatabase = new ArrayList<>();
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