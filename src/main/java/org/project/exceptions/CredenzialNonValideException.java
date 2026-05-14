package org.project.exceptions;

/**
 * Lanciata quando email/password non corrispondono a nessun utente
 * o quando il provider OAuth non è quello con cui l'utente si era registrato.
 */
public class CredenzialNonValideException extends Exception {

  public CredenzialNonValideException(String message) {
    super(message);
  }
}