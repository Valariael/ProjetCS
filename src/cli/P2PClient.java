/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */
package cli;

import comServCli.P2PFile;
import comServCli.P2PFunctions;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Axel Couturier
 */
public class P2PClient {

    private static ArrayList<P2PFile> listeFichiersLocaux;

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
            listeFichiersLocaux = new ArrayList<>();
            if (folder.isDirectory()) {
                for (final File fileEntry : folder.listFiles()) {
                    if (fileEntry.isDirectory()) {
                        // On n'affiche pas les dossiers
                    } else {
                        // Pour chaque fichier, on créé un P2PFile avec son nom et sa taille + on l'ajoutee a la liste.
                        // TODO : Possiblité d'optimisation : deux new file
                        listeFichiersLocaux.add(new P2PFile(args[2] + "\\" + fileEntry.getName()));
                        System.out.println(fileEntry.getName());
                    }
                }
            } else {
                System.out.println("L'argument indiqué n'est pas un dossier");
                System.exit(-1);
            }

            // Création du serverSocket d'écoute :
            ServerSocket sockEcoute = new ServerSocket(0);
            System.out.println("Port d'écoute : " + sockEcoute.getLocalPort());

            // FIXME : Debug only
            affichageMenu();

            // Connexion au serveur :
            Socket sockConnServer = new Socket();
            sockConnServer.connect(new InetSocketAddress(ipServ, portServ));
            ObjectOutputStream oos = new ObjectOutputStream(sockConnServer.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(sockConnServer.getInputStream());

            // Création du thread client :
            ThreadClient c = new ThreadClient(sockEcoute);
            c.start();

            // Transmettre au serveur la liste des fichiers locaux :
            oos.writeObject(listeFichiersLocaux);
            oos.flush();

            // Affichage du menu 
            affichageMenu();

            // Fermeture des flux du serveur 
            oos.close();
            oos = null;
            ois.close();
            oos = null;

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

    /**
     * Réalise l'affiche du menu de l'application cliente
     */
    public static void affichageMenu() {
        System.out.println("Que voulez vous faire ?");
        System.out.println("\t search <pattern>");
        System.out.println("\t get <num>");
        System.out.println("\t list");
        System.out.println("\t local list");
        System.out.println("\t quit");

        Scanner sc = new Scanner(System.in);
        String reponse = sc.nextLine();
        String[] reponse_ = reponse.split(" ");
        switch (reponse_[0]) {
            case "search":
                String pattern = reponse_[1];
                System.out.println("Pattern recherché :"+pattern);
                break;
            case "get":
                break;
            case "list":
                break;
            case "local":
                if (reponse_[1].equals("list")) {
                    System.out.println("Liste des fichiers que vous avez en local : ");
                    P2PFunctions.afficherListe(listeFichiersLocaux);
                } else {
                    System.out.println("Ceci n'est pas un choix !");
                    affichageMenu();
                }
                break;
            case "quit":

                System.exit(0);
                break;
            default:
                System.out.println("Ceci n'est pas un choix !");
                affichageMenu();
                break;
        }
    }
}
