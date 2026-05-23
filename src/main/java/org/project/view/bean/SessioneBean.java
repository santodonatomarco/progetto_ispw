package org.project.view.bean;

import java.util.List;

public class SessioneBean {
    private int id;
    private StudenteBean studente;
    private ProfessoreBean professore;
    private List<SchoolClassBean> listaClassi;
    private PortafoglioBean portafoglio;

    public SessioneBean(int id, StudenteBean studente){
        this.id = id;
        this.studente = studente;
    }

    public SessioneBean(int id, ProfessoreBean professore){
        this.id = id;
        this.professore = professore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StudenteBean getStudente() {
        return studente;
    }

    public void setStudente(StudenteBean studente) {
        this.studente = studente;
    }

    public ProfessoreBean getProfessore() {
        return professore;
    }

    public void setProfessore(ProfessoreBean professore) {
        this.professore = professore;
    }

    public List<SchoolClassBean> getListaClassi() {
        return listaClassi; }

    public void setListaClassi(List<SchoolClassBean> listaClassi) {
        this.listaClassi = listaClassi; }

    public PortafoglioBean getPortafoglio()                         { return portafoglio; }
    public void setPortafoglio(PortafoglioBean portafoglio)         { this.portafoglio = portafoglio; }

}