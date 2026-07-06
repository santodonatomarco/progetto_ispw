package org.project.view.bean;

public class UtenteBean {
    private final String email;
    private static final int MAX_LUNGHEZZA = 320;
    private static final java.util.regex.Pattern PATTERN_EMAIL =
            java.util.regex.Pattern.compile(
                    "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    public UtenteBean(String emailTarget) {
        if (emailTarget == null || emailTarget.isBlank())
            throw new IllegalArgumentException("L'email non può essere vuota.");

        String normalizzata = emailTarget.trim().toLowerCase();

        if (normalizzata.length() > MAX_LUNGHEZZA)
            throw new IllegalArgumentException(
                    "L'email supera la lunghezza massima consentita (" + MAX_LUNGHEZZA + " caratteri).");

        long numeroChiocciola = normalizzata.chars().filter(c -> c == '@').count();
        if (numeroChiocciola != 1)
            throw new IllegalArgumentException(
                    "L'email deve contenere esattamente una '@'.");

        String[] parti = normalizzata.split("@");
        if (parti[0].isBlank())
            throw new IllegalArgumentException(
                    "La parte prima di '@' dell'email non può essere vuota.");

        if (!parti[1].contains("."))
            throw new IllegalArgumentException(
                    "Il dominio dell'email deve contenere almeno un punto.");

        if (!PATTERN_EMAIL.matcher(normalizzata).matches())
            throw new IllegalArgumentException(
                    "Formato email non valido: " + emailTarget);

        this.email = normalizzata;
    }

    public String getEmail() {
        return email;
    }
}

