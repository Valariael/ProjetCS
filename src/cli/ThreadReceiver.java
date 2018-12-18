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
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
    private long end;

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
    }

    @Override
    public void run() {
        int np;
        String str;
        try {
            byte[] bufRequete = new byte[P2PParam.TAILLE_BUF + 8]; // TODO  : Change
            // le tableau bufRequete constitue le buffer de données du DatagramPacket pkRequete
            DatagramPacket dp = new DatagramPacket(bufRequete, bufRequete.length);
            System.out.println("DEBUG :  ThreadReceiver démarré, début de reception");
//            InetAddress addr = dp.getAddress();
//            int port = dp.getPort();


            boolean again = true;
            String nAck;
            long n;

            sockUDPReceive.setSoTimeout(1000);
            int i =0;
            while (i < P2PParam.NB_TENTATIVES_MAX) {
                long plusPetitPquetManquant = handleReceive();
                if (plusPetitPquetManquant == 0) {
                    System.out.println("DEBUG : Fin de transmission, réussie complétement.");
                    break;
                } 
                else {
                    System.out.println("Demande de réception des paquets depuis "+plusPetitPquetManquant);
                    System.out.println("DEBUG : Paquets manquants -> nouvel essai "+(i+1)+"/"+P2PParam.NB_TENTATIVES_MAX);
                }
                i++;
            }

            System.out.println("Fin du ThreadReceiver");

        } catch (IOException e) {
            System.out.println(e);
        }

        System.out.println("Fermeture du threadReceiver sur le port " + sockUDPReceive.getLocalPort());
    }

    public long handleReceive() throws IOException {
        byte[] bufRequete = new byte[P2PParam.TAILLE_BUF + 8];
        DatagramPacket dp = new DatagramPacket(bufRequete, bufRequete.length);
        sockUDPReceive.receive(dp);
        String str = new String(dp.getData(), 0, dp.getLength());
        int np = Integer.parseInt(str);

        long plusPetitPaquetManquant = -1;
        
        int nbTimeout = 0;

        // Création d'un tableau avec les numéros de tous les paquets attendus : 
        int nombrePaquets = (int) end - (int) start;
        ArrayList<Long> PaquetsArrives = new ArrayList<>();

        for (int i = 0; i < np; i++) {
            // Recoit les paquets et écrit dans le fichier de destination
            try {
                sockUDPReceive.receive(dp);
            } catch (SocketTimeoutException e) {
                System.out.println("DEBUG :: TIMEOUT DECLENCHE");
                nbTimeout++;
                if(nbTimeout >= 10) {
                    System.out.println("DEBUG : Nombre maximum de timeout atteint !");
                    break;
                }
                continue;
            }
            //System.out.println("DEBUG : Paquet " + i + "/" + np + "taille paquet reçu " + (dp.getLength() - 8));

            byte[] longforConv = new byte[8];
            System.arraycopy(dp.getData(), 0, longforConv, 0, 8);
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.put(longforConv);
            buffer.flip();//need flip 
            long position = buffer.getLong();
            Long numeroPaquetRecu = position / P2PParam.TAILLE_BUF;
            PaquetsArrives.add(numeroPaquetRecu);
            System.out.println("Paquet reçu : " + numeroPaquetRecu);

            byte[] dataToWrite = new byte[dp.getLength() - 8];

            System.arraycopy(dp.getData(), 8, dataToWrite, 0, dp.getLength() - 8);
            //System.out.println("DEBUG :  Data to write : " + Arrays.toString(dataToWrite));
            writeTo.write(position, dataToWrite);

            //System.out.println("DEBUG : Taille du fichier de destination : " + writeTo.getSize());
            //sockUDPReceive.send(retpacket);
        }

        // On va vérifier ce que l'on a reçu : 
        if (PaquetsArrives.size() != nombrePaquets) {
            System.out.println("DEBUG :  Nombre de paquets reçus différent de ce qui est attendu");
            // On cherche quels est le plus petit paquet qui nous manque.
            BouclePaquet:
            for (long i = 0; i < np; i++) {
                boolean arrive = false;
                for (Long PaquetArrive : PaquetsArrives) {
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
            System.out.println("SELON LA VERIFICATION LE PLUS PETIT PAQUET MANQUANT EST :::" + plusPetitPaquetManquant);

        } else {
            System.out.println("DEBUG : Tous les paquets semblent bien être arrivés");

        }
        return plusPetitPaquetManquant;
    }

}
