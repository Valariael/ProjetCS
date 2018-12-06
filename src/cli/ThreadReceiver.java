/*
 * LPRO 2018
 * Université de Franche-Comté
 */

package cli;

import java.net.DatagramSocket;

/**
 * Classe permettant la création d'un thread pour recevoir les données contenues dans le fichier du client détenteur.
 * 
 * @author Axel Couturier, Axel Ledermann
 */
public class ThreadReceiver extends Thread {
    private DatagramSocket sockUDPReceive;
    private ConcurrentFileStream writeTo;
    
    /**
     * Constructeur d'un ThreadReceiver.
     * 
     * @param ds le socket de réception des paquets UDP
     * @param cfs le pointeur vers le fichier à écrire
     */
    public ThreadReceiver(DatagramSocket ds, ConcurrentFileStream cfs) {
        this.sockUDPReceive = ds;
        this.writeTo = cfs;
    }
    
    @Override
    public void run() {
        // Recoit les paquets et écrit dans le fichier de destination
        // Utiliser la classe concurentfilestream;
        //sockUDPReceive.receive(pkRequete);
    }
    
}
