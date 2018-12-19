/*
 * LPRO 2018
 * Université de Franche-Comté
 */
package comServCli;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Liste les couples de P2PFile et de listes d'adresse de P2PClient. Sous classe
 * de HashMap.
 *
 * @author Axel Ledermann <axel.ledermann at univ-fcomte.org>
 */
public class ListFilesServer extends ConcurrentHashMap<P2PFile, ArrayList<AddressServer>> {

    /**
     * Constructeur de la liste des couples (P2PFile, liste d'adresses).
     */
    public ListFilesServer() {
        super();
    }

    /**
     * Recherche les P2PFile dont le nom de fichier correspond au nom passé en
     * paramètre.
     *
     * @param name le nom de fichier à rechercher
     * @return HashMap contenant les couples (P2PFile, liste d'adresses) dont le
     * nom de fichier contient "name"
     */
    public ListFilesServer searchPattern(String name) {
        ListFilesServer couplesSearched = null;
        Iterator it = this.entrySet().iterator();
        P2PFile file;
        System.out.println("DEBUG : Nombre de fichiers que sur lesquels on va chercher : "+this.size());
        // Parcourt les entrées de l'objet et vérifie pour chaque clé si le nom du P2PFile contient le pattern.
        while (it.hasNext()) {
            Map.Entry paire = (Map.Entry) it.next();
            file = (P2PFile) paire.getKey();
            if (file.getFilename().contains(name)) {
                if (couplesSearched == null) {
                    couplesSearched = new ListFilesServer();
                }
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
    public synchronized void updateList(ArrayList<P2PFile> newFiles, AddressServer client) {
        if (newFiles != null && newFiles.size() >= 1) {
            newFiles.forEach((f) -> {
                ArrayList<AddressServer> clientList = new ArrayList();
                if (this.get(f) != null) {
                    this.get(f).add(client);
                } else {
                    clientList.add(client);
                    this.put(f, clientList);
                }
            });
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

    @Override
    public String toString() {
        return super.toString();
    }

}
