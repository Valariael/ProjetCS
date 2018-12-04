/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */
package cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Axel Couturier
 */
public class ThreadClient extends Thread {

    private ServerSocket ss;

    public ThreadClient(ServerSocket ss) {
        this.ss = ss;
    }

    @Override
    public void run() {
        try {
            /* l’IP de l’hôte hébergeant l’application « P2PClient1 » ;
            - le numéro de port de la socket UDP « sockUdpReceive2 » qui va recevoir les
            morceaux du fichier de numéros compris dans l’intervalle [0, 5[ ;
            - le nom et la taille du fichier à télécharger ;
            - le numéro du premier morceau à envoyer (inclus) ;
            - le numéro du dernier morceau à envoyer (exclu) ;
             */
            Socket s = ss.accept();
            System.out.println("Connexion reçue");
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            
            // Reception d'une requete de téléchargement : 
            RequeteDownload r = (RequeteDownload)ois.readObject();
            ThreadSender ts = new ThreadSender(r);
            ts.run();
            

            /*
            1 - création d’une socket de communication TCP qui se connecte sur la socket
d’écoute TCP de l’application P2PClient possédant le fichier ;
2 - création d’une nouvelle socket UDP ;
3 - envoi d’une requête à l’application P2PClient en utilisant la socket de
communication TCP créée à l’étape 1;
4 - fermeture de la socket de communication TCP créée à l’étape 1 ;
5 - création d’un nouveau thread de type « ThreadReceiver ».
            
                attend une connexion,
                - instancie un BufferedReader et un PrintWriter grâce à la socket de communication obtenue,
                - attend une ligne de texte envoyée par le client et l'affiche à l'écran,
                - renvoie cette ligne au client. 
          
            System.out.println(plec.read());
            pw.write("test");
            pw.flush();
            System.out.println("Réponse envoyée au client..."); */
            s.close();
            s = null;
        } catch (IOException ex) {
            Logger.getLogger(ThreadClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ThreadClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
