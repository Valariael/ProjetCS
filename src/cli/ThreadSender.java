/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */ 

package cli;

import comServCli.AddressServer;
import comServCli.P2PFile;
import comServCli.P2PParam;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
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

    public ThreadSender(RequeteDownload r) {
        this.fichier = r.getFichier();
        this.premierMorceau = r.getPremierMorceau();
        this.dernierMorceau = r.getDernierMorceau();
        this.destinataire = r.getAdresseReceiver();
    }
    
    @Override
    public void run() {
        try {
            // Ouverture du fichier à lire.
            RandomAccessFile stream = null;
            try {
                stream = new RandomAccessFile(fichier.getFilename(), "r");
            } catch (FileNotFoundException e) {
                System.out.println(e); //TODO: gestion erreur
                System.exit(1);
            }
            
            long nAck = 0;
            boolean resend = true;
            byte[] bytes = new byte[P2PParam.TAILLE_BUF];
            DatagramPacket packetSend, packetReceive = null;
            DatagramSocket pkSender = new DatagramSocket();
            long nToBeSent = (dernierMorceau - premierMorceau);
            String str = nToBeSent +  "";
            try {
                while(resend) {
                    packetSend = new DatagramPacket(str.getBytes(), str.length(), new InetSocketAddress(destinataire.getHost(), destinataire.getPort()));
                    pkSender.send(packetSend);
                    
                    // On lit le fichier morceau par morceau.
                    for(long i=premierMorceau; i<dernierMorceau; i++) { //TODO : improve protocol
                        stream.seek(i*P2PParam.TAILLE_BUF);
                        stream.read(bytes);
                        packetSend = new DatagramPacket(bytes, bytes.length, new InetSocketAddress(destinataire.getHost(), destinataire.getPort()));
                        pkSender.send(packetSend);
                        
                        pkSender.receive(packetReceive);
                        str = new String(packetReceive.getData(), 0, packetReceive.getLength());
                        if(str.equals("recu")) nAck++;
                    }

                    System.out.println("DEBUG: ACKS " + nAck + " / " + nToBeSent);
                    if(nAck < nToBeSent) {
                        resend = true;
                        premierMorceau = nAck;
                        String tot = "" + (nAck - nToBeSent);
                        packetSend = new DatagramPacket(tot.getBytes(), tot.length(), new InetSocketAddress(destinataire.getHost(), destinataire.getPort()));
                        pkSender.send(packetSend);
                    } else {
                        resend = false;
                        packetSend = new DatagramPacket("0".getBytes(), 1, new InetSocketAddress(destinataire.getHost(), destinataire.getPort()));
                        pkSender.send(packetSend);
                    }
                }
                
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(ThreadSender.class.getName()).log(Level.SEVERE, null,ex); 
        }
    }
}
