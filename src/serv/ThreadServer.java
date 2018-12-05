/*
 * LPRO 2018
 * Université de Franche-Comté
 */
package serv;

import comServCli.AddressServer;
import comServCli.ListFilesServer;
import comServCli.P2PFile;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Axel Ledermann <axel.ledermann at univ-fcomte.org>
 */
public class ThreadServer extends Thread {
    
    private Socket sockComm;
    private ListFilesServer fileList;
    
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
                if (!ois.readBoolean()) {
                    break;
                }
                
                // Réception de la requête.
                request = (String) ois.readObject();
                System.out.println("Requête : " + request);
                
                // Vérification de la requête.
                // En cas d'erreur, lève une exception.
                requestParts = request.split(" ");
                if(requestParts[0].equals("search")) {
                    ListFilesServer filesSearched = fileList.searchPattern(requestParts[1]);
                    if(filesSearched == null) {
                        // throw no such file exception
                        oos.writeObject(null);
                    } else {
                        oos.writeObject(filesSearched);
                    }
                } else if(requestParts[0].equals("get")) {
                    P2PFile fileToGet = (P2PFile) ois.readObject();
                    //throw null pointer exception
                    
                    ArrayList<AddressServer> sources = fileList.getSources(fileToGet);
                    if(sources == null) {
                        //throw no sources exception
                    }
                    oos.writeObject(sources);
                } else {
                    //throw bad request exception
                    oos.writeObject(null);
                }
            }
            System.out.println("Fin du thread.");

            // Fermeture des flux et de la connexion avec le client courant.
            oos.close();
            oos = null;
            ois.close();
            ois = null;
            sockComm.close();
            sockComm = null;
        } catch (IOException e) {
            System.out.println(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ThreadServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
