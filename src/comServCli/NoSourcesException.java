/*
 * LPRO 2018
 * Université de Franche-Comté
 */
package comServCli;

/**
 * Classe d'exception, est lancée si un fichier demandé n'a aucune source.
 * 
 * @author Axel Ledermann <axel.ledermann at univ-fcomte.org>
 */
public class NoSourcesException extends Exception {

    /**
     * Le constructeur de l'exception NoSourcesException.
     * 
     * @param msg le message d'erreur
     */
    public NoSourcesException(String msg) {
        super(msg);
    }
}
