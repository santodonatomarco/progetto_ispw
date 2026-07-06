package org.project.view.bean;

public class ImpostaBudgetBean {
    private final double nuovoBudget;

    public ImpostaBudgetBean(double nuovoBudget) {
        if (nuovoBudget <= 0)
            throw new IllegalArgumentException("Il budget non può essere negativo.");
        this.nuovoBudget = nuovoBudget;
    }

    public double getNuovoBudget() {
        return nuovoBudget;
    }
}
