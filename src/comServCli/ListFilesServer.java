/*
 * LPRO 2018
 * Université de Franche-Comté
 */
package comServCli;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Liste les couples de P2PFile et de listes d'adresse de P2PClient
 * @author Axel Ledermann <axel.ledermann at univ-fcomte.org>
 */
public class ListFilesServer extends HashMap<P2PFile, List<String>> {
    
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
    public Map searchPattern(String name) {
        int nFiles = 0;
        Map<P2PFile, List<String>> couplesSearched;
        Iterator it = this.entrySet().iterator();
        P2PFile file;
        
        while (it.hasNext()) {
            Map.Entry paire = (Map.Entry)it.next();
            file = (P2PFile) paire.getKey();
            if(file.getFilename().equals(name)) {
                nFiles++;
            }
            it.remove(); // permet d'éviter ConcurrentModificationException
        }
        
        if(nFiles < 1) return null;
        couplesSearched = new HashMap<>(nFiles);
        
        while (it.hasNext()) {
            Map.Entry paire = (Map.Entry)it.next();
            file = (P2PFile) paire.getKey();
            if(file.getFilename().equals(name)) {
                couplesSearched.put(file, (List) paire.getValue());
            }
            it.remove(); // permet d'éviter ConcurrentModificationException
        }
        
        return couplesSearched;
    }
}
