package org.project.dao.messaggi;

import org.project.exceptions.DAOException;
import org.project.ing.persistenza.CachedDAO;
import org.project.model.Message;
import org.project.model.Utente;

import java.util.List;

public abstract class MessageDAO extends CachedDAO<Message> {

    @Override
    protected String ottieniChiave(Message m) {
        return m.getMittente().presentaEmail() + "_" + m.getTimestamp().toString();
    }


    public List<Message> getMessaggiRicevuti(Utente destinatario) throws DAOException {
        return doRetrieveMessaggiRicevuti(destinatario.presentaEmail());
    }

    public void salvaMessaggio(Message messaggio) throws DAOException {
        doSaveMessaggio(messaggio);
        addToCache(messaggio);
    }

    protected abstract List<Message> doRetrieveMessaggiRicevuti(String emailDestinatario) throws DAOException;
    protected abstract void doSaveMessaggio(Message messaggio) throws DAOException;
}