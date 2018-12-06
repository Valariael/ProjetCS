/*
 * LPRO 2018
 * Université de Franche-Comté
 */
package comServCli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Liste les couples de P2PFile et de listes d'adresse de P2PClient.
 * Sous classe de HashMap.
 * 
 * @author Axel Ledermann <axel.ledermann at univ-fcomte.org>
 */
public class ListFilesServer extends HashMap<P2PFile, ArrayList<AddressServer>> {
    
    /**
     * Constructeur de la liste des couples (P2PFile, liste d'adresses).
     */
    public ListFilesServer() {
        super();
    }
    
    /**
     * Recherche les P2PFile dont le nom de fichier correspond au nom passé en paramètre.
     * 
     * @param name le nom de fichier à rechercher
     * @return HashMap contenant les couples (P2PFile, liste d'adresses) dont le nom de fichier contient "name"
     */
    public ListFilesServer searchPattern(String name) {
        ListFilesServer couplesSearched = null;
        Iterator it = this.entrySet().iterator();
        P2PFile file;
        
        // Parcourt les entrées de l'objet et vérifie pour chaque clé si le nom du P2PFile contient le pattern.
        while (it.hasNext()) {
            Map.Entry paire = (Map.Entry)it.next();
            file = (P2PFile) paire.getKey();
            if(file.getFilename().contains(name)) {
                if(couplesSearched == null) couplesSearched = new ListFilesServer();
                // Ajout de l'entrée dans la liste à retourner.
                couplesSearched.put(file, (ArrayList) paire.getValue());
            }
            it.remove(); // permet d'éviter ConcurrentModificationException
        }
        
        return couplesSearched;
    }
    
    /**
     * Met à jour la liste de fichiers avec les fichiers du nouveau client.
     * 
     * @param newFiles la liste des nouveaux P2PFile
     * @param client l'adresse du client
     */
    public void updateList(ArrayList<P2PFile> newFiles, AddressServer client) {
        Iterator<Map.Entry<P2PFile, ArrayList<AddressServer>>> it = this.entrySet().iterator();
        P2PFile file;
        ArrayList<AddressServer> clientList = new ArrayList();
        
        // Si la liste est vide, ajoute directement les entrées.
        if(this.isEmpty()) {
            clientList.add(client);
            for(P2PFile f : newFiles) {
                this.put(f, clientList);
            }
            return;
        }
        
        // Pour chaque fichier déjà présent dans la liste, recherche une correspondance avec les fichiers passés en paramètre.
        while (it.hasNext()) {
            Map.Entry<P2PFile, ArrayList<AddressServer>> paire = it.next();
            file = paire.getKey();
            
            for(P2PFile f : newFiles) {
                if(file.equals(f)) {
                    this.remove(file);
                    clientList = paire.getValue();
                }
            }
            
            clientList.add(client);
            this.put(file, clientList);
            it.remove(); // permet d'éviter ConcurrentModificationException
        }
    }
    
    /**
     * Trouve les adresses des clients possédant la fichier "file".
     * 
     * @param file le fichier concerné
     * @return la liste des adresses des clients possédant le fichier
     */
    public ArrayList<AddressServer> getSourcesFromFile(P2PFile file) {
        return this.get(file);
    }
}
