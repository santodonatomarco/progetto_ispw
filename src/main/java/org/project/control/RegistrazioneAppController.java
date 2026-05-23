package org.project.control;

import org.project.dao.professori.ProfessoreDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.exceptions.ControllerException;
import org.project.exceptions.CredenzialNonValideException;
import org.project.exceptions.DAOException;
import org.project.ing.classifunzionali.Hasher;
import org.project.ing.enumerations.AuthProvider;
import org.project.ing.persistenza.DAOFactory;
import org.project.model.*;
import org.project.view.bean.ProfessoreBean;
import org.project.view.bean.SessioneBean;
import org.project.view.bean.StudenteBean;

public class RegistrazioneAppController {

    // ── Registrazione studente ────────────────────────────────────────────────

    /**
     * Registrazione studente.
     * Il bean deve contenere: email, nome, cognome, nomeClasse, password.
     * authProvider nel bean indica il metodo: LOCAL (default) o OAuth (non implementato).
     */
    public SessioneBean registraStudente(StudenteBean bean)
            throws CredenzialNonValideException, ControllerException {

        DAOFactory factory = DAOFactory.getDAOFactory();
        factory.createSchoolClassDAO();
        StudenteDAO studenteDAO = factory.createStudenteDAO();

        try {
            Studente pending = studenteDAO.getStudenteByEmail(bean.getEmail());

            if (pending == null) {
                throw new CredenzialNonValideException(
                        "Nessun account pending trovato per questa email. " +
                                "Chiedi al tuo professore di aggiungerti alla classe.");
            }

            if (pending.classeFrequentata() == null ||
                    !pending.classeFrequentata().nome().equals(bean.getNomeClasse())) {
                throw new CredenzialNonValideException(
                        "La classe indicata non corrisponde a quella associata al tuo account.");
            }

            AuthProvider provider = (bean.getAuthProvider() != null)
                    ? bean.getAuthProvider() : AuthProvider.LOCAL;

            switch (provider) {
                case LOCAL -> pending.impostaPasswordHash(Hasher.codifica(bean.getPassword()));
                case GOOGLE, MICROSOFT -> throw new CredenzialNonValideException(
                        "Registrazione tramite " + provider + ": caso d'uso non implementato.");
                default -> throw new CredenzialNonValideException("Provider non supportato.");
            }

            pending.chiamaNome(bean.getNome());
            pending.chiamaCognome(bean.getCognome());

            studenteDAO.salvaStudente(pending);

            Sessione sessione = SessionManager.getInstance().creaSessione(pending);

            StudenteBean studenteBean = new StudenteBean(
                    pending.presentaEmail(),
                    pending.presentaNome(),
                    pending.presentaCognome()
            );
            studenteBean.resetPassword();

            return new SessioneBean(sessione.getToken(), studenteBean);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante la registrazione dello studente.", e);
        }
    }

    // ── Registrazione professore ──────────────────────────────────────────────

    /**
     * Registrazione professore.
     * Il bean deve contenere: email, nome, cognome, password.
     * authProvider nel bean indica il metodo: LOCAL (default) o OAuth (non implementato).
     */
    public SessioneBean registraProfessore(ProfessoreBean bean)
            throws CredenzialNonValideException, ControllerException {

        DAOFactory factory = DAOFactory.getDAOFactory();
        ProfessoreDAO professoreDAO = factory.createProfessoreDAO();

        try {
            Professore esistente = professoreDAO.getProfessoreByEmail(bean.getEmail());
            if (esistente != null) {
                throw new CredenzialNonValideException(
                        "Esiste già un account registrato con questa email.");
            }

            AuthProvider provider = (bean.getAuthProvider() != null)
                    ? bean.getAuthProvider() : AuthProvider.LOCAL;

            Professore nuovo = new Professore(bean.getEmail(), bean.getNome(), bean.getCognome(), provider);

            switch (provider) {
                case LOCAL -> nuovo.impostaPasswordHash(Hasher.codifica(bean.getPassword()));
                case GOOGLE, MICROSOFT -> throw new CredenzialNonValideException(
                        "Registrazione tramite " + provider + ": caso d'uso non implementato.");
                default -> throw new CredenzialNonValideException("Provider non supportato.");
            }

            professoreDAO.salvaProfessore(nuovo);

            Sessione sessione = SessionManager.getInstance().creaSessione(nuovo);

            ProfessoreBean professoreBean = new ProfessoreBean(
                    nuovo.presentaEmail(),
                    nuovo.presentaNome(),
                    nuovo.presentaCognome()
            );

            return new SessioneBean(sessione.getToken(), professoreBean);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante la registrazione del professore.", e);
        }
    }
}
