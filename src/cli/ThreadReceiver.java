/*
 * LPRO 2018
 * Université de Franche-Comté
 */
package cli;

import comServCli.P2PParam;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Classe permettant la création d'un thread pour recevoir les données contenues
 * dans le fichier du client détenteur. ;
 *
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
        this.start = start * P2PParam.TAILLE_BUF;
    }

    @Override
    public void run() {
        int np;
        String str;

        try {
            // Réception du nombre de paquets total
            byte[] bufRequete = new byte[P2PParam.TAILLE_BUF+8]; // TODO  : Change
            // Constructs a DatagramPacket for receiving packets of length length.
            // le tableau bufRequete constitue le buffer de données du DatagramPacket pkRequete
            DatagramPacket dp = new DatagramPacket(bufRequete, bufRequete.length);
            System.out.println("DEBUG :  ThreadReceiver démarré, début de reception");
            sockUDPReceive.receive(dp);
            InetAddress addr = dp.getAddress();
            int port = dp.getPort();
            str = new String(dp.getData(), 0, dp.getLength());
            np = Integer.parseInt(str);
            System.out.println("DEBUG : Contenu de str :" + str);

            boolean again = true;
            String nAck;
            long n;
            DatagramPacket retpacket = new DatagramPacket("recu".getBytes(), 4, addr, port);
            while (again) {
                for (int i = 0; i < np; i++) {
                    // Recoit les paquets et écrit dans le fichier de destination
                    sockUDPReceive.receive(dp);
                    System.out.println("DEBUG : Paquet " + i + "/" + np +"taille paquet reçu "+(dp.getLength()-8));
                    byte[] longforConv = new byte[8];
                    System.arraycopy(dp.getData(), 0, longforConv, 0, 8);
                    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                    buffer.put(longforConv);
                    buffer.flip();//need flip 
                    long position = buffer.getLong();
                    
                    byte[] dataToWrite = new byte[dp.getLength()-8];
                    
   
                    System.arraycopy(dp.getData(), 8, dataToWrite, 0, dp.getLength()-8);
                    System.out.println("DEBUG :  Data to write : "+Arrays.toString(dataToWrite));
                    //System.out.println("DEBUG : Theadreceiver : Position : " + position);
                    writeTo.write(position, dataToWrite);
                    
                    System.out.println("DEBUG : Taille du fichier de destination : "+writeTo.getSize());

                    sockUDPReceive.send(retpacket);
                    //System.out.println("DEBUG: ACK sent");
                }

                sockUDPReceive.receive(dp);
                nAck = new String(dp.getData(), 0, dp.getLength());
                n = Long.parseLong(nAck);
                System.out.println("DEBUG: nAck=" + n);
                // TODO : Fin d'envoi ??
                //if(str.equals("0")) again = false;
                again = false;
            }
            System.out.println("Fin du ThreadReceiver");

        } catch (IOException e) {
            System.out.println(e);
        }

        System.out.println("Fermeture du threadReceiver sur le port " + sockUDPReceive.getLocalPort());
    }

}
