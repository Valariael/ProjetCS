/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */
package cli;

import comServCli.AddressServer;
import comServCli.BadRequestException;
import comServCli.ListFilesServer;
import comServCli.NoSuchFileException;
import comServCli.P2PFile;
import comServCli.P2PFunctions;
import comServCli.P2PParam;
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

/**
 *
 * @author Axel Couturier
 */
public class P2PClient {

    private static ArrayList<P2PFile> listeFichiersLocaux;
    private static ListFilesServer resultatsRecherche;
    private static ArrayList<P2PFile> downloadableFiles = null;

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
        try {

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
            
            // On va récuperer notre IP locale depuis le serveur car on ne sait pas laquelle est utilisée : 
            InetAddress myIP = (InetAddress) ois.readObject();
            System.out.println("DEBUG : Selon le serveur, votre ip est "+myIP.getHostAddress());
            
            // Création du thread client :
            ThreadClient c = new ThreadClient(sockEcoute, folder); // passer la liste des fichiers locaux en objet partagé ?
            c.start();

            // Transmettre au serveur le port local et la liste des fichiers locaux:
            oos.writeInt(sockEcoute.getLocalPort());
            oos.flush();
            oos.writeObject(listeFichiersLocaux);
            oos.flush();

            String[] reponse_;
            do {
                // Affichage du menu, récupération de l'entrée utilisateur et vérification de la taille de la requête
                try {
                    request = requestMenu();
                    reponse_ = request.split(" ");
                    if(reponse_.length > 3 || reponse_.length < 1) throw new BadRequestException("Requête malformée..");
                } catch(BadRequestException e) {
                    System.out.println(e);
                    continue;
                }
                
                // Application de la requête suivant les différentes possibilités
                switch (reponse_[0]) {
                    case "search":
                        if(reponse_.length > 2) {
                            System.out.println("Le pattern recherché ne doit pas contenir d'espaces..");
                            continue;
                        }
                        if(reponse_.length < 2) {
                            System.out.println("Veuillez écrire la séquence recherchée après \"search\"..");
                            continue;
                        }
                        String pattern = reponse_[1];
                        System.out.println("Pattern recherché : " + pattern);
                        oos.writeBoolean(end);
                        oos.flush();
                        oos.writeObject(request);
                        oos.flush();
                        
                        
                        // On envoie la liste des fichiers locaux avant chaque requête pour s'assurer de la justesse du retour.
                        oos.writeObject(listeFichiersLocaux);
                        oos.flush();

                        Object o = ois.readObject();
                        if(o == null) {
                            System.out.println("Aucun résultat pour votre recherche...");
                        } else {
                            resultatsRecherche = (ListFilesServer) o;
                            System.out.println("Liste des fichiers correspondants à votre recherche :\n");
                            downloadableFiles = P2PFunctions.setToArrayList(resultatsRecherche.keySet());
                            P2PFunctions.afficherListe(downloadableFiles, true);
                        }
                        break;
                    case "get":
                        ArrayList<AddressServer> sources = null;
                        RequeteDownload r = null;
                        // check that num is in range
                        
                        try {
                            if(resultatsRecherche.isEmpty()) throw new NoSuchFileException("Commencez par rechercher un fichier avec \"search <pattern>\"..");
                            int choix = Integer.parseInt(reponse_[1]) - 1; // -1 pour matcher avec la liste
                            P2PFile fichierADL = downloadableFiles.get(choix);
                            if(listeFichiersLocaux != null) {
                                if(listeFichiersLocaux.contains(fichierADL)) throw new FileAlreadyLocalException("Inutile de télécharger ce fichier, il est déjà présent sur le disque..");
                            }
                            sources = resultatsRecherche.getSourcesFromFile(fichierADL);
                            
                            int nClients = sources.size();
                            long fileSize = fichierADL.getSize();
                            long nombreMorceaux = (long) Math.ceil((double)fileSize/P2PParam.TAILLE_BUF);
                            
                            long chunkStart, chunkEnd;
                            long nbMorceauxParClient = (long) Math.ceil(nombreMorceaux/nClients);
                            System.out.println("DEBUG: nClients=" + nClients + ", filesize=" + fileSize + ", nbMorceau="+nombreMorceaux+", nbMorceauxParClient="+nbMorceauxParClient);
                            
                            ConcurrentFileStream cfs = new ConcurrentFileStream(folder, fichierADL);

                            ObjectOutputStream roos = null;
                            ObjectInputStream rois = null;
                            
                            // Découpe du fichier en x morceau :
                            long MorceauxAttribues = 0;
                            // Création des ThreadReceiver : 
                            for (int i = 0; i < nClients; i++) {
                                try {
                                    // Répartition entre les différents clients qui disposent de ce fichier : 
                                    DatagramSocket sockUDPReceive = new DatagramSocket();
                                    chunkStart = MorceauxAttribues;
                                    chunkEnd = MorceauxAttribues+nbMorceauxParClient;
                                    if(i == (nClients-1)) {
                                        // Si on est sur le dernier client :
                                        chunkEnd = nombreMorceaux;
                                    }
                                    MorceauxAttribues =+ nbMorceauxParClient;
                                    System.out.println("DEBUG : chunkStart: "+chunkStart+" chunkEnd: "+chunkEnd);
                                    
                                    // Création du socket pour communiquer la requête de téléchargement au client détenteur du fichier
                                    Socket socket = new Socket();
                                    // Connexion au socket du client hôte n°i
                                    System.out.println("DEBUG : Connexion au socket du client hote suivant : "+sources.get(i).getHost()+":"+ sources.get(i).getPort());
                                    socket.connect(new InetSocketAddress(sources.get(i).getHost(), sources.get(i).getPort()));
                                   
                                    // On lie la requête au socket récepteur des paquets UDP et on y ajoute le P2PFile correspondant au fichier demandé aisni que les octets de début et de fin
                                    r = new RequeteDownload(new AddressServer(myIP.getHostAddress(), sockUDPReceive.getLocalPort()), fichierADL, chunkStart, chunkEnd);
                                    // On envoie la requête au client hôte
                                    try {
                                        roos = new ObjectOutputStream(socket.getOutputStream());
                                        roos.flush();
                                        rois = new ObjectInputStream(socket.getInputStream());

                                        roos.writeObject(r);
                                        roos.flush();

                                        if (rois.readBoolean()) {
                                            ThreadReceiver tr = new ThreadReceiver(sockUDPReceive, cfs, chunkStart, chunkEnd);
                                            tr.start();
                                            
                                            // incrémenter les variables
                                        } else {
                                            // envoyer la requete a un autre client ?
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        System.out.println(e);
                                    } finally {
                                        try {
                                            roos.close();
                                            roos = null;
                                            rois.close();
                                            rois = null;
                                            socket.close();
                                        } catch(IOException e) {
                                            System.out.println(e);
                                        }
                                    }

                                } catch (SocketException ex) {
                                    System.out.println("Erreur lors de la connexion au socket d'écoute d'un client");
                                    System.out.println(ex.getMessage());
                                }
                            }
                            
                            // Fermeture de l'écriture du fichier
                            //cfs.close();

                        } catch (NumberFormatException e) {
                            System.out.println("Vous n'avez pas entré un nombre ! ");
                        } catch (FileAlreadyLocalException | NoSuchFileException e) {
                            System.out.println(e);
                        }
                        break;
                    case "list":
                        try {
                            if(reponse_.length > 1) throw new BadRequestException("Veuillez écrire \"list\" uniquement..");
                            System.out.println("Liste des fichiers de votre dernière recherche : ");
                            P2PFunctions.afficherListe(downloadableFiles, true);
                        } catch (BadRequestException e) {
                            System.out.println(e);
                        }
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
            oos.flush();
            oos.writeObject(listeFichiersLocaux);
            oos.flush();

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
            System.out.println("wrote");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(e);
        } finally {
            // Fermeture du socket.
            if (sockConnServer != null) {
                try {
                    sockConnServer.close();
                    System.exit(0);
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
        if(!resultatsRecherche.isEmpty()) {
            System.out.println("\t get <num>");
        }
        if(downloadableFiles != null) {
            System.out.println("\t list");
        }
        System.out.println("\t local list");
        System.out.println("\t quit");

        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
