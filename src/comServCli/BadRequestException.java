/*
 * LPRO 2018
 * Université de Franche-Comté
 */
package comServCli;

/**
 * Classe d'exception, est lancée si la requête formée est incorrecte.
 * 
 * @author Axel Ledermann <axel.ledermann at univ-fcomte.org>
 */
public class BadRequestException extends Exception {

    /**
     * Le constructeur de l'exception BadRequestException.
     * 
     * @param msg le message d'erreur
     */
    public BadRequestException(String msg) {
        super(msg);
    }
}
