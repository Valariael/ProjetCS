/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
 */
package cli;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Thread intermédiaire précédant l'envoi des données lors d'un téléchargement.
 * Réceptionne la requête de téléchargement et crée le ThreadSender qui enverra les données.
 * 
 * @author Axel Couturier
 */
public class ThreadClient extends Thread {

    private ServerSocket ss;
    private File folder;

    /**
     * Constructeur principal du ThreadClient.
     * 
     * @param ss le ServerSocket d'où proviendra la requête de téléchargement
     * @param folder le répertoire dans lequel sera écrit le fichier
     */
    public ThreadClient(ServerSocket ss, File folder) {
        this.ss = ss;
        this.folder = folder;
    }

    @Override
    public void run() {
        try {
            // Attente d'une connexion d'un autre client.
            Socket initSocket = ss.accept();
            System.out.println("Connexion reçue");
            ObjectOutputStream oos = new ObjectOutputStream(initSocket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(initSocket.getInputStream());

            // Reception d'une requete de téléchargement : 
            RequeteDownload rd = (RequeteDownload) ois.readObject();
            oos.writeBoolean(true);
            oos.flush();
            ThreadSender ts = new ThreadSender(rd, folder);
            ts.start();

            ois.close();
            oos.close();
            initSocket.close();
            initSocket = null;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
}
