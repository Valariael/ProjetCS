/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */
package cli;

import comServCli.AddressServer;
import comServCli.ListFilesServer;
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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Axel Couturier
 */
public class P2PClient {

    private static ArrayList<P2PFile> listeFichiersLocaux;
    private static ListFilesServer resultatsRecherche;
    private static ArrayList<P2PFile> downloadableFiles;
    private static AddressServer localAddress;

    public static void main(String[] args) {
        resultatsRecherche = new ListFilesServer();
        // Vérification des arguments du programme.
        if (args.length != 3) {
            System.out.println("Usage : java P2PClient IPServeur portServeur répertoireDuClient");
            System.exit(1);
        }
        InetAddress ipServ = null;
        try {
            ipServ = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
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
        final File folder = new File(args[2]);
        if (!folder.isDirectory()) {
            System.out.println("Le chemin entré n'est pas un dossier !");
            System.exit(1);
        }

        boolean end = false;
        String request;
        Socket sockConnServer = null;
//        DatagramSocket sockComm = null;
        try {
//            sockComm = new DatagramSocket();
//            // TODO : Modifier pour l'adresse du serveur
//            sockComm.connect(InetAddress.getByName("8.8.8.8"), 10002);
//            localAddress = sockComm.getLocalAddress();
//           System.out.println("Adresse locale : " + sockComm.getLocalAddress().getHostAddress() + ":" + sockComm.getLocalPort());

            // Dans un premier temps on va lister les fichiers qu'il y a dans le dossier  :
            listeFichiersLocaux = P2PFunctions.getLocalFiles(folder);
            if (listeFichiersLocaux == null) {
                System.out.println("DEBUG : Pas de fichiers locaux.");
            }

            // Création du serverSocket d'écoute :
            ServerSocket sockEcoute = new ServerSocket(0);
            System.out.println("DEBUG : Port d'écoute : " + sockEcoute.getLocalPort());

            // Connexion au serveur et instanciation des flux.
            sockConnServer = new Socket();
            sockConnServer.connect(new InetSocketAddress(ipServ, portServ));
            ObjectOutputStream oos = new ObjectOutputStream(sockConnServer.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(sockConnServer.getInputStream());

            // Création du thread client :
            ThreadClient c = new ThreadClient(sockEcoute); // passer la liste des fichiers locaux en objet partagé ?
            c.start();

            // Transmettre au serveur la liste des fichiers locaux + AddressServeur du socketDecoute local:
            oos.writeInt(sockEcoute.getLocalPort());
            oos.flush();
            oos.writeObject(listeFichiersLocaux);
            oos.flush();

            // Affichage du menu
            do {
                request = requestMenu();
                String[] reponse_ = request.split(" ");
                switch (reponse_[0]) {
                    case "search":
                        // TODO: ne pas autoriser les requetes avec plus de x espaces
                        //TODO: malformed request exception
                        // TODO : Si le tableau est trop petit (search vide)
                        String pattern = reponse_[1];
                        System.out.println("Pattern recherché :" + pattern);
                        oos.writeBoolean(end);
                        oos.flush();
                        oos.writeObject(request);
                        oos.flush();

                        resultatsRecherche = (ListFilesServer) ois.readObject();
                        System.out.println("Liste des fichiers correspondants à votre recherche :\n");
                        //TODO: handle null liste
                        //optimisation: changer listfilesserver en treemap
                        downloadableFiles = P2PFunctions.setToArrayList(resultatsRecherche.keySet());
                        P2PFunctions.afficherListe(downloadableFiles, true);
                        break;
                    case "get":
                        //TODO: verifier que le fichier n'est pas déjà present sur le client

                        //TODO: cas de la recherche non faite
                        // Envoi d'une requête au serveur pour obtenir la liste des IPs
                        ArrayList<AddressServer> sources = null;
                        RequeteDownload r = null;
                        // check that num is in range
                        // get sources and file from num associated with listfilesserver

                        try {
                            int choix = Integer.parseInt(reponse_[1]) - 1; // -1 pour matcher avec la liste
                            P2PFile fichierADL = downloadableFiles.get(choix);
                            sources = resultatsRecherche.getSourcesFromFile(fichierADL);
                            int nClients = sources.size();
                            long fileSize = fichierADL.getSize();

                            long chunkStart = 0, chunkEnd;
                            //  RequeteDownload[] fileDLRequests = new RequeteDownload[nClients];
                            System.out.println("DEBUG: nClients=" + nClients + ", filesize=" + fileSize);
                            for (int i = 0; i < nClients; i++) {
                                System.out.println(sources.get(i));
                            }

                            // Découpe du fichier en x morceau :
                            // Répartition entre les différents clients qui disposent de ce fichier : 
                            // Création du concurrentfilestream pour que plusieurs clients puissent écrire :
                            ConcurrentFileStream cfs = new ConcurrentFileStream(fichierADL);

                            ObjectOutputStream roos = null;
                            ObjectInputStream rois = null;
                            // Création des ThreadReceiver : 
                            for (int i = 0; i < nClients; i++) {
                                try {
                                    DatagramSocket sockUDPReceive = new DatagramSocket();
                                    chunkEnd = fileSize / nClients; // à modifier 

                                    // Création du socket pour communiquer la requête de téléchargement au client détenteur du fichier
                                    Socket socket = new Socket();
                                    // Connexion au socket du client hôte n°i
                                    System.out.println("DEBUG : Connexion au socket du client hote suivant : "+sources.get(i).getHost()+":"+ sources.get(i).getPort());
                                    socket.connect(new InetSocketAddress(sources.get(i).getHost(), sources.get(i).getPort()));
                                   
                                    // On lie la requête au socket récepteur des paquets UDP et on y ajoute le P2PFile correspondant au fichier demandé aisni que les octets de début et de fin
                                    r = new RequeteDownload(new AddressServer(sockUDPReceive.getLocalAddress().getHostAddress(), sockUDPReceive.getLocalPort()), fichierADL, chunkStart, chunkEnd);
                                    // On envoie la requête au client hôte
                                    try {
                                        roos = new ObjectOutputStream(socket.getOutputStream());
                                        roos.flush();
                                        rois = new ObjectInputStream(socket.getInputStream());

                                        roos.writeObject(r);
                                        roos.flush();

                                        if (rois.readBoolean()) {
                                            ThreadReceiver tr = new ThreadReceiver(sockUDPReceive, cfs);
                                            tr.start();
                                            // incrémenter les variables
                                        } else {
                                            // envoyer la requete a un autre client ?
                                        }

                                        roos.close();
                                        roos = null;
                                        rois.close();
                                        rois = null;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        System.out.println(e);
                                    }

                                } catch (SocketException ex) {
                                    System.out.println("Erreur lors de la création du socket");
                                    System.out.println(ex.getMessage());
                                }
                            }

                        } catch (NumberFormatException e) {
                            System.out.println("Vous n'avez pas entré un nombre ! ");
                        }
                        break;
                    case "list":
                        System.out.println("Liste des fichiers de votre dernière recherche : ");
                        P2PFunctions.afficherListe(downloadableFiles, true);
                        break;
                    case "local":
                        if (reponse_[1].equals("list")) {
                            System.out.println("Liste des fichiers que vous avez en local : ");
                            P2PFunctions.afficherListe(listeFichiersLocaux);
                        } else {
                            System.out.println("Ceci n'est pas un choix !");
                        }
                        break;
                    case "quit":
                        end = true;
                        break;
                    default:
                        System.out.println("Ceci n'est pas un choix !");
                        break;
                }
            } while (!end);

            oos.writeBoolean(end);

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
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(e);
        } finally {
            // close flux
            if (sockConnServer != null) {
                try {
                    sockConnServer.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    /**
     * Réalise l'affiche du menu de l'application cliente.
     *
     * @return la String entrée par l'utilisateur
     */
    public static String requestMenu() {
        System.out.println("Que voulez vous faire ?");
        System.out.println("\t search <pattern>");
        System.out.println("\t get <num>"); // afficher l'option get num uniquement si une liste des fichiers  été récupérée
        System.out.println("\t list");
        System.out.println("\t local list");
        System.out.println("\t quit");

        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
