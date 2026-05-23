package org.project.control;

import org.project.dao.classi.SchoolClassDAO;
import org.project.dao.studenti.StudenteDAO;
import org.project.dao.wallets.PortafoglioDAO;
import org.project.exceptions.ControllerException;
import org.project.exceptions.DAOException;
import org.project.ing.persistenza.DAOFactory;
import org.project.model.*;
import org.project.view.bean.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller applicativo per la gestione classe da parte del professore.
 *
 * Responsabilità:
 *  - Crea una nuova classe assegnata al professore loggato
 *  - Imposta/modifica il budget iniziale di una classe esistente
 *  - Legge l'elenco delle classi del professore
 */
public class GestioneClasseAppController {

    /**
     * Crea una nuova classe con il budget dato e la assegna al professore in sessione.
     */
    public SchoolClassBean creaClasse(SessioneBean sessione, String nomeClasse, double budgetIniziale)
            throws ControllerException {

        if (nomeClasse == null || nomeClasse.isBlank())
            throw new ControllerException("Il nome della classe non può essere vuoto.");
        if (budgetIniziale < 0)
            throw new ControllerException("Il budget non può essere negativo.");

        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null)
            throw new ControllerException("Sessione non valida o scaduta.");

        Professore professore = sessioneModel.getProfessorCorrente();
        if (professore == null)
            throw new ControllerException("Solo i professori possono creare classi.");

        DAOFactory factory = DAOFactory.getDAOFactory();
        SchoolClassDAO classeDAO = factory.createSchoolClassDAO();

        try {
            // Verifica che la classe non esista già per questo professore
            SchoolClass esistente = classeDAO.getClasseByNomeEProfessore(nomeClasse, professore);
            if (esistente != null)
                throw new ControllerException("Esiste già una classe \"" + nomeClasse + "\" per questo professore.");

            SchoolClass nuovaClasse = new SchoolClass(nomeClasse, professore);
            nuovaClasse.impostaBudget(budgetIniziale);
            classeDAO.salvaClasse(nuovaClasse);

            return toBean(nuovaClasse);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante la creazione della classe.", e);
        }
    }

    /**
     * Aggiorna il budget iniziale di una classe esistente
     * e aggiorna RETROATTIVAMENTE i portafogli di tutti gli studenti iscritti.
     */
    public SchoolClassBean impostaBudget(SessioneBean sessione, String nomeClasse, double nuovoBudget)
            throws ControllerException {

        if (nuovoBudget < 0)
            throw new ControllerException("Il budget non può essere negativo.");

        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null)
            throw new ControllerException("Sessione non valida o scaduta.");

        Professore professore = sessioneModel.getProfessorCorrente();
        if (professore == null)
            throw new ControllerException("Solo i professori possono modificare il budget.");

        DAOFactory factory = DAOFactory.getDAOFactory();

        // IMPORTANTE: Manteniamo quest'ordine per evitare le dipendenze circolari!
        SchoolClassDAO classeDAO = factory.createSchoolClassDAO();
        StudenteDAO studenteDAO = factory.createStudenteDAO();
        PortafoglioDAO portafoglioDAO = factory.createPortafoglioDAO();

        try {
            SchoolClass classe = classeDAO.getClasseByNomeEProfessore(nomeClasse, professore);
            if (classe == null)
                throw new ControllerException("Classe \"" + nomeClasse + "\" non trovata.");

            // 1. Calcoliamo la differenza di budget
            double vecchioBudget = classe.budgetIniziale();
            double differenza = nuovoBudget - vecchioBudget;

            // 2. Aggiorniamo la classe e la salviamo
            classe.impostaBudget(nuovoBudget);
            classeDAO.salvaClasse(classe);

            // 3. Retroattività: Modifichiamo i portafogli degli studenti
            if (differenza != 0) {
                List<Studente> studenti = studenteDAO.getStudentiClasse(classe);

                if (studenti != null) {
                    for (Studente s : studenti) {
                        VirtualWallet wallet = portafoglioDAO.getPortafoglioByEmail(s.presentaEmail());

                        if (wallet != null) {
                            if (differenza > 0) {
                                // Il budget è aumentato: facciamo un bonifico extra
                                wallet.accreditaSaldo(differenza);
                            } else {
                                // Il budget è diminuito: recuperiamo i soldi.
                                // Usiamo Math.abs per avere un numero positivo da scalare
                                double daScalare = Math.abs(differenza);

                                // Controllo di sicurezza: se lo studente ha già speso tutto,
                                // scaliamo solo quello che gli è rimasto per evitare crash
                                if (wallet.saldoDisponibile() >= daScalare) {
                                    wallet.scalaSaldo(daScalare);
                                } else {
                                    wallet.scalaSaldo(wallet.saldoDisponibile()); // Lo lasciamo a zero
                                }
                            }
                            // Salviamo il portafoglio aggiornato nel database
                            portafoglioDAO.salvaPortafoglio(wallet);
                        }
                    }
                }
            }

            // 4. Aggiorna anche la sessione corrente se è la classe selezionata
            SchoolClass classeInSessione = sessioneModel.getClasseCorrente();
            if (classeInSessione != null && classeInSessione.nome().equals(nomeClasse)) {
                classeInSessione.impostaBudget(nuovoBudget);
            }

            return toBean(classe);

        } catch (DAOException e) {
            throw new ControllerException("Errore durante l'aggiornamento del budget o dei portafogli.", e);
        }
    }

    /**
     * Recupera la lista delle classi del professore loggato.
     */
    public List<SchoolClassBean> getClassiDelProfessore(SessioneBean sessione) throws ControllerException {
        Sessione sessioneModel = SessionManager.getInstance().ottieniSessione(sessione.getId());
        if (sessioneModel == null)
            throw new ControllerException("Sessione non valida o scaduta.");

        Professore professore = sessioneModel.getProfessorCorrente();
        if (professore == null)
            throw new ControllerException("Solo i professori possono accedere alle classi.");

        DAOFactory factory = DAOFactory.getDAOFactory();
        SchoolClassDAO classeDAO = factory.createSchoolClassDAO();

        try {
            List<SchoolClass> classi = classeDAO.getClassiByProfessore(professore);
            List<SchoolClassBean> beans = new ArrayList<>();
            for (SchoolClass c : classi) beans.add(toBean(c));
            return beans;
        } catch (DAOException e) {
            throw new ControllerException("Errore nel recupero delle classi.", e);
        }
    }

    // ── Conversione model → bean ──────────────────────────────────────────────

    private SchoolClassBean toBean(SchoolClass c) {
        ProfessoreBean profBean = new ProfessoreBean(
                c.teacher().presentaEmail(),
                c.teacher().presentaNome(),
                c.teacher().presentaCognome());
        List<StudenteBean> studentiBeans = new ArrayList<>();
        if (c.studenti() != null) {
            for (Studente s : c.studenti()) {
                studentiBeans.add(new StudenteBean(
                        s.presentaEmail(), s.presentaNome(), s.presentaCognome()));
            }
        }
        return new SchoolClassBean(c.nome(), profBean, c.budgetIniziale(), studentiBeans);
    }
}