/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
 */
package comServCli;

/**
 * Classe contenant des paramètres d'ordre général pour le projet.
 * 
 * @author Axel Couturier
 */
public class P2PParam {

    /**
     * Taille maximum des buffers des DatagramPacket permettant l'envoi des fichiers sous forme d'octets.
     */
    public static final int TAILLE_BUF = 1024;

    /**
     * Temps d'attente maximum après envoi d'un DatagramPacket.
     */
    public static final int TIMEOUT_UDP = 30;
    
    /**
     * Correspond au nombre maximal de tentatives d'envoi que l'on va réaliser avant d'abandonner.
     */
    public static final int NB_TENTATIVES_MAX = 5;

}
