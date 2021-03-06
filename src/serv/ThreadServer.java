/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
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
     * @param sockComm le socket de communication du serveur lié au client connecté
     * @param fileList la liste des fichiers accessibles par le serveur
     */
    public ThreadServer(Socket sockComm, ListFilesServer fileList) {
        this.sockComm = sockComm;
        this.fileList = fileList;
    }
    
    @Override
    public void run() {
        AddressServer client = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            // Instanciation des flux.
            ois = new ObjectInputStream(sockComm.getInputStream());
            oos = new ObjectOutputStream(sockComm.getOutputStream());
            oos.flush();
            
            // On renvoie directement l'ip du client car c'est le seul moment ou on la connait :
            oos.writeObject(sockComm.getInetAddress());
            oos.flush();
            
            // Création des variables et de l'AddressServer correspondant au client connecté
            int portSockEcoute = ois.readInt();
            client = new AddressServer(sockComm.getInetAddress().getHostAddress(), portSockEcoute);
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
                
                clientFiles = (ArrayList<P2PFile>) ois.readObject();
                fileList.updateList(clientFiles, client);
                
                // Vérification de la requête.
                // En cas d'erreur, lève une exception.
                requestParts = request.split(" ");
                if(requestParts.length < 1) {
                    continue;
                }
                if(requestParts[0].equals("search")) {
                    // Recherche le pattern dans la liste des fichiers disponibles.
                    ListFilesServer filesSearched = fileList.searchPattern(requestParts[1]);
                    System.out.println("DEBUG, fichiers recherche " + filesSearched);
                    
                    // Écriture de la liste des fichiers correspondant.
                    try {
                        if(filesSearched == null) {
                            throw new NoSuchFileException("Aucun fichier trouvé..");
                        }
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
                } else {
                    try {
                        throw new BadRequestException("Requête incorrecte..");
                    } catch (BadRequestException e) {
                        System.out.println(e);
                        oos.writeObject(null);
                        oos.flush();
                    }
                }
            }
            
            // Réception de la liste des fichiers locaux du client pour les enlever de la liste des sources.
             ArrayList<P2PFile> filesToRemove = (ArrayList<P2PFile>) ois.readObject();
             fileList.removeList(filesToRemove, client);
            System.out.println("DEBUG, Fin du thread.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        } finally {
            // Fermeture des flux et de la connexion avec le client courant.
            try {
                if(oos != null) {
                    oos.close();
                    oos = null;
                }
                if(ois != null) {
                    ois.close();
                    ois = null;
                }
                if(sockComm != null) {
                    sockComm.close();
                    sockComm = null;
                }
            } catch(IOException e) {
                System.out.println(e);
            }
        }
    }
    
}
