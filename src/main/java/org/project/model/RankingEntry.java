package org.project.model;

public class RankingEntry {

    private StudenteOAuth student;
    private int posizione;
    private double valorePortafoglio;
    private double guadagnoSettimanalePercent;

    public RankingEntry(StudenteOAuth student, int posizione,
                        double valorePortafoglio, double guadagnoPercent) {
        this.collegaStudent(student);
        this.impostaPosizione(posizione);
        this.impostaValore(valorePortafoglio);
        this.impostaGuadagno(guadagnoPercent);
    }

    public final void collegaStudent(StudenteOAuth student) {
        if (student == null)
            throw new IllegalArgumentException("Lo studente non può essere nullo.");
        this.student = student;
    }

    public final void impostaPosizione(int posizione) {
        if (posizione < 1)
            throw new IllegalArgumentException("La posizione deve essere almeno 1.");
        this.posizione = posizione;
    }

    public final void impostaValore(double valore) {
        if (valore < 0)
            throw new IllegalArgumentException("Il valore non può essere negativo.");
        this.valorePortafoglio = valore;
    }

    public final void impostaGuadagno(double percent) {
        this.guadagnoSettimanalePercent = percent;
    }

    public StudenteOAuth student()                    { return student; }
    public int posizione()                      { return posizione; }
    public double valorePortafoglio()           { return valorePortafoglio; }
    public double guadagnoSettimanalePercent()  { return guadagnoSettimanalePercent; }
}