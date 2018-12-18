/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */
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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                System.out.println(e); //TODO: gestion erreur
                System.exit(1);
            }

            long nAck = 0;
            boolean resend = true;
            byte[] bytes; // On ajoute un long qui contient la position
            byte[] bufRequete = new byte[8000]; // TODO  : Change
            DatagramPacket packetSend, packetReceive = null;
            packetReceive = new DatagramPacket(bufRequete, bufRequete.length);
            DatagramSocket pkSender = new DatagramSocket();
            long nToBeSent = (dernierMorceau - premierMorceau);
            String str = nToBeSent + "";
            try {
                while (resend) {
                    packetSend = new DatagramPacket(str.getBytes(), str.length(), new InetSocketAddress(destinataire.getHost(), destinataire.getPort()));
                    pkSender.send(packetSend);

                    // On lit le fichier morceau par morceau.
                    for (long i = premierMorceau; i < dernierMorceau; i++) {
                        if(i==1997) {
                            continue;
                        }
                        System.out.println("Envoi du morceau"+i+"/"+dernierMorceau);
                        long position = i * P2PParam.TAILLE_BUF;
                        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                        buffer.putLong(position);
                        stream.seek(position);

                        // On va vérifier que c'est n'est pas le dernier morceau du fichier que l'on envoie  :
                        long fileSize = fichier.getSize();
                        long nombreMorceaux = (long) Math.ceil((double) fileSize / P2PParam.TAILLE_BUF);
                        long tailleDernierMorceau = fileSize % P2PParam.TAILLE_BUF;
                        System.out.println("i="+i+", fileSize="+fileSize+", nombreMorceaux="+nombreMorceaux+", tailleDernirMOrceau="+tailleDernierMorceau);
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
                        //System.out.println("DEBUG : Paquet envoyé : " + Arrays.toString(bytes));
                        pkSender.send(packetSend);

                        //pkSender.receive(packetReceive);
                        resend = false;
                    }

                    System.out.println("DEBUG: ACKS " + nAck + " / " + (nToBeSent + 1));
                    /*if (nAck < nToBeSent) {
                        resend = true;
                        premierMorceau = nAck;
                        String tot = "" + (nAck - nToBeSent);
                        packetSend = new DatagramPacket(tot.getBytes(), tot.length(), new InetSocketAddress(destinataire.getHost(), destinataire.getPort()));
                        pkSender.send(packetSend);
                    } else {
                        resend = false;
                        packetSend = new DatagramPacket("0".getBytes(), 1, new InetSocketAddress(destinataire.getHost(), destinataire.getPort()));
                        pkSender.send(packetSend);
                    } */
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
        } catch (SocketException ex) {
            Logger.getLogger(ThreadSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
