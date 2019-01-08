/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
 */
package cli;

import comServCli.AddressServer;
import comServCli.P2PFile;
import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * @author Axel Couturier
 */
public class RequeteDownload implements Serializable {
    private AddressServer adresseReceiver;
    private InetAddress demandeur;
    private P2PFile fichier;
    private long premierMorceau;
    private long dernierMorceau;

    public RequeteDownload(AddressServer adresseReceiver, InetAddress demandeur, P2PFile fichier, long premierMorceau, long dernierMorceau) {
        this.adresseReceiver = adresseReceiver;
        this.demandeur = demandeur;
        this.fichier = fichier;
        this.premierMorceau = premierMorceau;
        this.dernierMorceau = dernierMorceau;
    }
    
    public RequeteDownload(AddressServer adresseReceiver, P2PFile fichier, long premierMorceau, long dernierMorceau) {
        this.adresseReceiver = adresseReceiver;
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

    public long getPremierMorceau() {
        return premierMorceau;
    }

    public long getDernierMorceau() {
        return dernierMorceau;
    }
    
    
}
