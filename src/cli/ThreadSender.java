/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */ 

package cli;

import comServCli.AddressServer;
import comServCli.P2PFile;

/**
 *
 * @author Axel Couturier
 */
public class ThreadSender extends Thread {
    
    private P2PFile fichier;
    private long premierMorceau;
    private long dernierMorceau;
    private AddressServer destinataire;

    public ThreadSender(RequeteDownload r) {
        this.fichier = r.getFichier();
        this.premierMorceau = r.getPremierMorceau();
        this.dernierMorceau = r.getDernierMorceau();
        this.destinataire = r.getAdresseReceiver();
    }
    
    

    public void run() {
        System.out.println("DEBUG : Démarrage d'un threadSender à destination de "+destinataire);
    }
}
