/*
 * LPRO 2018
 * Université de Franche-Comté
 */
package serv;

import comServCli.ListFilesServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe principale pour le serveur P2P.
 *
 * @author Axel Ledermann <axel.ledermann at univ-fcomte.org>
 */
public class P2PServer {

    public static void main(String[] args) {
        // Vérification des arguments du programme.
        if (args.length != 1) {
            System.out.println("Usage : java P2PServer portServeur");
            System.exit(1);
        }

        int portServ = 0;
        try {
            portServ = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Numéro de port non valide !");
            System.exit(1);
        }
        if (portServ < 1024 || portServ > 65535) {
            System.out.println("Numéro de port non autorisé ou non valide !");
            System.exit(1);
        }

        ServerSocket sockConn = null;
        Socket sockComm = null;
        ListFilesServer fileList = new ListFilesServer();

        try {
            // Création du socket serveur.
            sockConn = new ServerSocket(portServ);

            while (true) {
                sockComm = null;
                // Attend une connexion d'un client.
                sockComm = sockConn.accept();

                System.out.println("DEBUG, Connexion d'un client, adresse de la socket distante : " + sockComm.getInetAddress().getHostAddress() + ":" + sockComm.getPort());
                // Lance un thread pour le client connecté.
                ThreadServer t = new ThreadServer(sockComm, fileList);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        } finally {
            try {
                if (sockComm != null) {
                    sockComm.close();
                }
                if (sockConn != null) {
                    sockConn.close();
                }
            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }
}
