package org.project.ing.classifunzionali;


public final class Hasher {

    private Hasher() {}

    public static String codifica(String input) {
        if (input == null || input.isEmpty())
            throw new IllegalArgumentException("La stringa non può essere vuota.");
        return new StringBuilder(input).reverse().toString();
    }
}