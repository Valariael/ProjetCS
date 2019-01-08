/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
 */
package cli;

import comServCli.AddressServer;
import comServCli.P2PFile;
import comServCli.P2PParam;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

/**
 *
 * @author Axel Couturier
 */
public class ThreadSender extends Thread {

    private P2PFile fichier;
    private long premierMorceau;
    private long dernierMorceau;
    private AddressServer destinataire;
    private File folder;

    public ThreadSender(RequeteDownload r, File folder) {
        this.fichier = r.getFichier();
        this.premierMorceau = r.getPremierMorceau();
        this.dernierMorceau = r.getDernierMorceau();
        this.destinataire = r.getAdresseReceiver();
        this.folder = folder;
    }

    @Override
    public void run() {
        try {
            System.out.println("Démarrage d'un threadSender a destination de " + this.destinataire);
            // Ouverture du fichier à lire.
            RandomAccessFile stream = null;
            try {
                stream = new RandomAccessFile(folder + "\\" + fichier.getFilename(), "r");
            } catch (FileNotFoundException e) {
                System.out.println(e);
                System.exit(1);
            }

            byte[] bytes; // On ajoute un long qui contient la position
            byte[] bufRequete = new byte[8000]; // TODO  : Change
            DatagramPacket packetSend, packetReceive = null;
            packetReceive = new DatagramPacket(bufRequete, bufRequete.length);
            DatagramSocket pkSender = new DatagramSocket();
            ByteBuffer buffer;
            long nToBeSent = (dernierMorceau - premierMorceau);
            String str = nToBeSent + "";
            try {
                while (true) {
                    packetSend = new DatagramPacket(str.getBytes(), str.length(), new InetSocketAddress(destinataire.getHost(), destinataire.getPort()));
                    pkSender.send(packetSend);

                    // On lit le fichier morceau par morceau.
                    for (long i = premierMorceau; i < dernierMorceau; i++) {
                        System.out.println("Envoi du morceau" + i + "/" + dernierMorceau);
                        long position = i * P2PParam.TAILLE_BUF;
                        buffer = ByteBuffer.allocate(Long.BYTES);
                        buffer.putLong(position);
                        stream.seek(position);

                        // On va vérifier que c'est n'est pas le dernier morceau du fichier que l'on envoie  :
                        long fileSize = fichier.getSize();
                        long nombreMorceaux = (long) Math.ceil((double) fileSize / P2PParam.TAILLE_BUF);
                        long tailleDernierMorceau = fileSize % P2PParam.TAILLE_BUF;

                        if (i == (nombreMorceaux - 1)) {
                            System.out.println("DEBUG  : Traitement du dernier morceau à l'envoi");
                            bytes = new byte[8 + (int) tailleDernierMorceau];
                            stream.read(bytes, 8, (int) tailleDernierMorceau);
                        } else {
                            bytes = new byte[P2PParam.TAILLE_BUF + 8];
                            stream.read(bytes, 8, P2PParam.TAILLE_BUF);
                        }
                        System.arraycopy(buffer.array(), 0, bytes, 0, 8);

                        packetSend = new DatagramPacket(bytes, bytes.length, new InetSocketAddress(destinataire.getHost(), destinataire.getPort()));
                        pkSender.send(packetSend);
                    }
                    pkSender.setSoTimeout(15000);
                    try {
                        pkSender.receive(packetReceive);
                        byte[] data = packetReceive.getData();
                        String sLenght = new String(data).trim();
                        Long retour = Long.valueOf(sLenght);
                        if (retour == -1) {
                            //  Si il n'en manque aucun :
                            System.out.println("DEBUG : FIN DE TRANSMISSION IL NE MANQUE AUCUN PAQUET");
                            break;
                        } else {
                            System.out.println("DEBUG : DEMANDE DE RE EMISSION DEPUIS " + retour);
                            premierMorceau = retour;

                        }
                    } catch (SocketTimeoutException e) {
                        System.out.println("DEBUG : Timeout a la reception");
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("DEBUG : ThreadSender : Echec de l'envoi");
                System.out.println(e);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        } catch (SocketException e) {
            System.out.println(e);
        }
        System.out.println("Fermeture du threadSender");
    }
}
