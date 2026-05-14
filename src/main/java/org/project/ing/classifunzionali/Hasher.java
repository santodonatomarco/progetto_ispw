package org.project.ing.classifunzionali;


/**
 * Utility per la codifica delle password in sviluppo.
 * Inverte la stringa: "password" → "drowssap".
 *
 * DA SOSTITUIRE con bcrypt o Argon2 prima di andare in produzione.
 */
public class Hasher {

    private Hasher() {}

    public static String codifica(String input) {
        if (input == null || input.isEmpty())
            throw new IllegalArgumentException("La stringa non può essere vuota.");
        return new StringBuilder(input).reverse().toString();
    }
}