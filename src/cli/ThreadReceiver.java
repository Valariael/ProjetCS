/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
 */
package cli;

import comServCli.P2PParam;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Classe permettant la création d'un thread pour recevoir les données contenues
 * dans le fichier du client détenteur. ;
 *
 * @author Axel Couturier
 */
public class ThreadReceiver extends Thread {

    private DatagramSocket sockUDPReceive;
    private ConcurrentFileStream writeTo;
    private long start;
    private long end;
    private SocketAddress addressSender;
    

    /**
     * Constructeur du ThreadReceiver.
     *
     * @param ds le socket de réception des paquets UDP
     * @param cfs le pointeur vers le fichier à écrire
     * @param start la postion du dernier chunk
     * @param end dernier chunk
     */
    public ThreadReceiver(DatagramSocket ds, ConcurrentFileStream cfs, long start, long end) {
        this.sockUDPReceive = ds;
        this.writeTo = cfs;
        this.start = start;
        this.end = end;
        this.addressSender = null;
    }

    @Override
    public void run() {
        int np;
        String str;
        
        try {
            System.out.println("DEBUG :  ThreadReceiver démarré, début de reception");

            sockUDPReceive.setSoTimeout(1000);
            int i = 0;
            while (i < P2PParam.NB_TENTATIVES_MAX) {
                long plusPetitPquetManquant = handleReceive();
                // On envoie un message pour demander une ré-emission : 
                String taille;
                if(plusPetitPquetManquant == -1) {
                    taille = -1+"";
                }
                else {
                    taille = (plusPetitPquetManquant+start)+"";
                }
                
                this.start = (plusPetitPquetManquant+start);
                byte[] bufRequete = taille.getBytes();
                
                DatagramPacket dp = new DatagramPacket(bufRequete, bufRequete.length, addressSender);
                sockUDPReceive.send(dp);
                if (plusPetitPquetManquant == -1) {
                    System.out.println("DEBUG : Fin de transmission, réussie complétement.");
                    break;
                } else {
                    System.out.println("Demande de réception des paquets depuis " + (plusPetitPquetManquant+start));
                    System.out.println("DEBUG : Paquets manquants -> nouvel essai " + (i + 1) + "/" + P2PParam.NB_TENTATIVES_MAX);
                }
                i++;
            }

            System.out.println("Fin du ThreadReceiver");

        } catch (IOException e) {
            System.out.println(e);
        }

        System.out.println("Fermeture du threadReceiver sur le port " + sockUDPReceive.getLocalPort());
    }

    /**
     * Réalise la réception d'un fichier
     * @return le nombre de paquets manquants ou -1 si il ne manque aucun paquet
     * @throws IOException 
     */
    public long handleReceive() throws IOException {
        byte[] bufRequete = new byte[P2PParam.TAILLE_BUF + 8];
        DatagramPacket dp = new DatagramPacket(bufRequete, bufRequete.length);
        sockUDPReceive.receive(dp);
        addressSender = dp.getSocketAddress();
        String str = new String(dp.getData(), 0, dp.getLength());
        int np = Integer.parseInt(str);

        long plusPetitPaquetManquant = -1;

        int nbTimeout = 0;

        // Création d'un tableau avec les numéros de tous les paquets attendus : 
        int nombrePaquets = (int) end - (int) start;
        ArrayList<Long> paquetsArrives = new ArrayList<>();

        for (int i = 0; i < np; i++) {
            // Recoit les paquets et écrit dans le fichier de destination
            try {
                sockUDPReceive.receive(dp);
            } catch (SocketTimeoutException e) {
                System.out.println("DEBUG :: TIMEOUT DECLENCHE");
                nbTimeout++;
                if (nbTimeout >= 10) {
                    System.out.println("DEBUG : Nombre maximum de timeout atteint !");
                    break;
                }
                continue;
            }
            
            byte[] longforConv = new byte[8];
            System.arraycopy(dp.getData(), 0, longforConv, 0, 8);
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.put(longforConv);
            buffer.flip();//need flip 
            long position = buffer.getLong();
            Long numeroPaquetRecu = position / P2PParam.TAILLE_BUF;
            paquetsArrives.add(numeroPaquetRecu);
            System.out.println("Paquet reçu : " + numeroPaquetRecu);

            byte[] dataToWrite = new byte[dp.getLength() - 8];

            System.arraycopy(dp.getData(), 8, dataToWrite, 0, dp.getLength() - 8);
            writeTo.write(position, dataToWrite);
        }

        // On va vérifier ce que l'on a reçu : 
        if (paquetsArrives.size() != np) {
            System.out.println("DEBUG : Il manque "+(nombrePaquets-paquetsArrives.size())+" paquets");
            // On cherche quel est le plus petit paquet qui nous manque.
            BouclePaquet:
            for (long i = 0; i < np; i++) {
                boolean arrive = false;
                for (Long PaquetArrive : paquetsArrives) {
                    if (PaquetArrive == i) {
                        arrive = true;
                        break;
                    }
                }
                if (!arrive) {
                    plusPetitPaquetManquant = i;
                    break;
                }
            }

        }
        return plusPetitPaquetManquant;
    }

}
