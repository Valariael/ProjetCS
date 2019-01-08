/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
 */
package comServCli;

/**
 * Classe d'exception, est lancée si un fichier recherché n'existe pas.
 * 
 * @author Axel Ledermann <axel.ledermann at univ-fcomte.org>
 */
public class NoSuchFileException extends Exception {

    /**
     * Le constructeur de l'exception NoSuchFileException.
     * 
     * @param msg le message d'erreur
     */
    public NoSuchFileException(String msg) {
        super(msg);
    }
}
