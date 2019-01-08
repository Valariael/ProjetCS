/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
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
