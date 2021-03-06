/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
 */
package cli;

/**
 * Classe d'exception, est lancée si le client demande le téléchargement d'un fichier déjà présent localement.
 *
 * @author Axel Ledermann <axel.ledermann at univ-fcomte.org>
 */
public class FileAlreadyLocalException extends Exception {
    /**
     * Le constructeur de l'exception FileAlreadyLocalException.
     * 
     * @param msg le message d'erreur
     */
    public FileAlreadyLocalException(String msg) {
        super(msg);
    }
}
