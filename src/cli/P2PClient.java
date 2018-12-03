/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */
package cli;

import comServCli.P2PFile;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 *
 * @author Axel Couturier
 */
public class P2PClient {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage : java P2PClient IPServeur portServeur dossierContentantlesFichiers");
            System.exit(1);
        }
        InetAddress ipServ = null;
        try {
            ipServ = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e1) {
            System.out.println("Le serveur n'existe pas !");
            System.exit(1);
        }
        int portServ = 0;
        try {
            portServ = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Numéro de port non valide !");
            System.exit(1);
        }
        if (portServ < 1024 || portServ > 65535) {
            System.out.println("Numéro de port non autorisé ou non valide !");
            System.exit(1);
        }

        DatagramSocket sockComm = null;
        try {
            sockComm = new DatagramSocket();
            // TODO : Modifier pour l'adresse du serveur
            sockComm.connect(InetAddress.getByName("8.8.8.8"), 10002);
            System.out.println("Adresse locale : " + sockComm.getLocalAddress().getHostAddress() + ":" + sockComm.getLocalPort());

            // Dans un premier temps on va lister les fichiers qu'il y a dans le dossier  :
            final File folder = new File(args[2]);
            ArrayList<P2PFile> listeFichiersLocaux = new ArrayList<>();
            if (folder.isDirectory()) {
                for (final File fileEntry : folder.listFiles()) {
                    if (fileEntry.isDirectory()) {
                        // On n'affiche pas les dossiers
                    } else {
                        // Pour chaque fichier, on créé un P2PFile avec son nom et sa taille + on l'ajoutee a la liste.
                        // TODO : Possiblité d'optimisation : deux new file
                        listeFichiersLocaux.add(new P2PFile(fileEntry.getName()));
                        System.out.println(fileEntry.getName());
                    }
                }
            } else {
                System.out.println("L'argument indiqué n'est pas un dossier");
                System.exit(-1);
            }

            // Création du serverSocket d'écoute :
            ServerSocket sockEcoute = new ServerSocket(0);
            System.out.println("Port d'écoute : "+sockEcoute.getLocalPort());
            
            // Connexion au serveur :
            Socket sockConnServer = new Socket();
            sockConnServer.connect(new InetSocketAddress(ipServ, portServ));

//            sockComm.send(pkRequete);
//            bufRequete = new byte[100];
//
//            pkRequete = new DatagramPacket(bufRequete, bufRequete.length);
//            sockComm.receive(pkRequete);
//            System.out.println("Requete reçue en retour : " + new String(bufRequete, 0, pkRequete.getLength(), Charset.defaultCharset()));
//
//            System.out.println("Envoi du Hello");
//            bufRequete = ((String) "Hello").getBytes();
//            pkRequete.setData(bufRequete);
//            sockComm.send(pkRequete);
//
//            bufRequete = new byte[100];
//            pkRequete = new DatagramPacket(bufRequete, bufRequete.length);
//            sockComm.receive(pkRequete);
//            System.out.println("Requete reçue en retour du Hello : " + new String(bufRequete, 0, pkRequete.getLength(), Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (sockComm != null) {
                sockComm.close();
            }
        }
    }

}
