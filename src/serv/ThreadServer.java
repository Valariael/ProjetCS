/*
 * LPRO 2018
 * Université de Franche-Comté
 */
package serv;

import comServCli.AddressServer;
import comServCli.BadRequestException;
import comServCli.ListFilesServer;
import comServCli.NoSourcesException;
import comServCli.NoSuchFileException;
import comServCli.P2PFile;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Sous processus interprétant les requêtes du client précédant le traitement du P2P.
 * Échange les listes d'adresses avec le client.
 * 
 * Les threads partagent la liste des couples fichier/liste d'adresses des hôtes.
 * 
 * @author Axel Ledermann <axel.ledermann at univ-fcomte.org>
 */
public class ThreadServer extends Thread {
    
    private Socket sockComm;
    private ListFilesServer fileList;
    
    /**
     * Constructeur de la classe ThreadServer.
     * 
     * @param comm le socket de communication du serveur lié au client connecté
     * @param fileList la liste des fichiers accessibles par le serveur
     */
    public ThreadServer(Socket comm, ListFilesServer fileList) {
        this.sockComm = comm;
        this.fileList = fileList;
    }
    
    @Override
    public void run() {
        try {
            // Instanciation des flux.
            ObjectInputStream ois = new ObjectInputStream(sockComm.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(sockComm.getOutputStream());
            oos.flush();
            
            // Création des variables et de l'AddressServer correspondant au client connecté
            AddressServer client = new AddressServer(sockComm.getInetAddress().getHostAddress(), sockComm.getPort());
            String request;
            String[] requestParts;
            
            // Réception de la liste des fichiers
            ArrayList<P2PFile> clientFiles = (ArrayList<P2PFile>) ois.readObject();
            
            // Mise a jour de la liste des fichiers sur le serveur
            fileList.updateList(clientFiles, client);
            // Lecture de chaque requête.
            while (true) {
                // Si le client n'envoie plus d'objet, quitter la boucle.
                if (ois.readBoolean()) {
                    break;
                }
                
                // Réception de la requête.
                request = (String) ois.readObject();
                System.out.println("DEBUG, Requête : " + request);
                
                // Vérification de la requête.
                // En cas d'erreur, lève une exception.
                requestParts = request.split(" ");
                if(requestParts[0].equals("search")) {
                    // Recherche le pattern dans la liste des fichiers disponibles.
                    ListFilesServer filesSearched = fileList.searchPattern(requestParts[1]);
                    System.out.println("DEBUG, fichiers recherche " + filesSearched);
                    
                    // Écriture de la liste des fichiers correspondant.
                    try {
                        if(filesSearched == null) throw new NoSuchFileException("Aucun fichier trouvé..");
                    } catch (NoSuchFileException e) {
                        System.out.println(e);
                    }
                    oos.writeObject(filesSearched);
                    oos.flush();
                } else if(requestParts[0].equals("get")) {
                    // Lecture du P2PFile (Map.Key) à télécharger.
                    P2PFile fileToGet = (P2PFile) ois.readObject();
                    if(fileToGet == null) throw new NullPointerException("Aucun fichier à rechercher..");
                    
                    // Récupère la liste des sources possibles pour le fichier demandé.
                    ArrayList<AddressServer> sources = fileList.getSourcesFromFile(fileToGet);
                    try {
                        if(sources == null) {
                            throw new NoSourcesException("Aucune source pour ce fichier..");
                        }
                    } catch (NoSourcesException e) {
                        System.out.println(e);
                    }
                    oos.writeObject(sources);
                    oos.flush();
                } else { // TODO : enhance exceptions
                    try {
                        throw new BadRequestException("Requête incorrecte..");
                    } catch (BadRequestException e) {
                        System.out.println(e);
                        oos.writeObject(null); // TODO:gestion du retour d'erreur !!!!!!!!!!
                        oos.flush();
                    }
                }
            }
            System.out.println("DEBUG, Fin du thread.");

            // Fermeture des flux et de la connexion avec le client courant.
            oos.close();
            oos = null;
            ois.close();
            ois = null;
            sockComm.close();
            sockComm = null;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }
    }
    
}
