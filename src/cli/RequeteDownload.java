/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */ 

package cli;

import comServCli.AddressServer;
import comServCli.P2PFile;
import java.net.InetAddress;

/**
 *
 * @author Axel Couturier
 */
public class RequeteDownload {
    private AddressServer adresseReceiver;
    private InetAddress demandeur;
    private P2PFile fichier;
    private int premierMorceau;
    private int dernierMorceau;

    public RequeteDownload(AddressServer receiver, AddressServer sender, P2PFile fichier, int premierMorceau, int dernierMorceau) {
        this.adresseReceiver = adresseReceiver;
        this.demandeur = demandeur;
        this.fichier = fichier;
        this.premierMorceau = premierMorceau;
        this.dernierMorceau = dernierMorceau;
    }

    public AddressServer getAdresseReceiver() {
        return adresseReceiver;
    }

    public InetAddress getDemandeur() {
        return demandeur;
    }

    public P2PFile getFichier() {
        return fichier;
    }

    public int getPremierMorceau() {
        return premierMorceau;
    }

    public int getDernierMorceau() {
        return dernierMorceau;
    }
    
    
}
