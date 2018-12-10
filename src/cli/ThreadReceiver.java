/*
 * LPRO 2018
 * Université de Franche-Comté
 */

package cli;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Classe permettant la création d'un thread pour recevoir les données contenues dans le fichier du client détenteur.
 * ;
 * @author Axel Couturier, Axel Ledermann
 */
public class ThreadReceiver extends Thread {
    private DatagramSocket sockUDPReceive;
    private ConcurrentFileStream writeTo;
    private long start;
    
    /**
     * Constructeur du ThreadReceiver.
     * 
     * @param ds le socket de réception des paquets UDP
     * @param cfs le pointeur vers le fichier à écrire
     * @param start la postion de début
     */
    public ThreadReceiver(DatagramSocket ds, ConcurrentFileStream cfs, long start) {
        this.sockUDPReceive = ds;
        this.writeTo = cfs;
        this.start = start;
    }
    
    @Override
    public void run() {
        DatagramPacket dp = null;
        int np;
        String str;
        
        try {
            // Réception du nombre de paquets total
            sockUDPReceive.receive(dp);
            InetAddress addr = dp.getAddress();
            int port = dp.getPort();
            str = new String(dp.getData(), 0, dp.getLength());
            np = Integer.parseInt(str);
            
            DatagramPacket retpacket = new DatagramPacket("recu".getBytes(), 4, addr, port);
            
            for(int i=0; i<np; i++) {
                // Recoit les paquets et écrit dans le fichier de destination
                sockUDPReceive.receive(dp);
                writeTo.write(start, dp.getData());
                
                sockUDPReceive.send(retpacket);
            }
            
        } catch (IOException e) {
            System.out.println(e);
        }
        
        
    }
    
}
