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
 * Liste les couples de P2PFile et de listes d'adresse de P2PClient
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
     * @param name le nom de fichier à rechercher.
     * @return HashMap contenant les couples (P2PFile, liste d'adresses) dont le nom de fichier
     */
    public ListFilesServer searchPattern(String name) {
        boolean match = false;
        ListFilesServer couplesSearched;
        Iterator it = this.entrySet().iterator();
        P2PFile file;
        
        couplesSearched = new ListFilesServer(); // double boucle pas tres jojo
        
        while (it.hasNext()) {
            Map.Entry paire = (Map.Entry)it.next();
            file = (P2PFile) paire.getKey();
            if(file.getFilename().contains(name)) {
                match = true;
                couplesSearched.put(file, (ArrayList) paire.getValue());
            }
            it.remove(); // permet d'éviter ConcurrentModificationException
        }
        if(!match) return null;
        
        return couplesSearched;
    }
    
    public void updateList(ArrayList<P2PFile> newFiles, AddressServer client) {
        Iterator<Map.Entry<P2PFile, ArrayList<AddressServer>>> it = this.entrySet().iterator();
        ListFilesServer newFileList = new ListFilesServer();
        P2PFile file;
        ArrayList<AddressServer> clientList;
        
        while (it.hasNext()) {
            clientList = new ArrayList();
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
    
    public ArrayList<AddressServer> getSources(P2PFile file) {
        return this.get(file);
    }
}
