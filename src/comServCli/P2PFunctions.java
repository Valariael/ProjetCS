/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
 */
package comServCli;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Classe contenant les fonctions de traitement pour le client et le serveur.
 *
 * @author Axel Couturier
 */
public class P2PFunctions {

    /**
     * Réalise l'affichage non numéroté d'une liste de fichiers.
     *
     * @param listeFichiers liste des fichiers
     */
    public static void afficherListe(ArrayList<P2PFile> listeFichiers) {
        afficherListe(listeFichiers, false);
    }

    /**
     * Réalise l'affichage d'une liste de fichiers, numérotée ou non
     *
     * @param listeFichiers : Liset des fichiers a afficher
     * @param numerotee : active la numérotation
     */
    public static void afficherListe(ArrayList<P2PFile> listeFichiers, boolean numerotee) {
        int i = 0;
        if (listeFichiers != null) {
            if (listeFichiers.size() >= 1) {
                for (P2PFile fichier : listeFichiers) {
                    i++;
                    System.out.println("\t " + (numerotee ? i + ". " : "- ") + fichier.toString());
                }
            } else {
                System.out.println("Il n'y a aucun résultat..");
            }
        } else {
            System.out.println("\tListe vide..");
        }
    }

    /**
     * Recherche tous les fichiers présents dans le dossier passé en paramètre.
     *
     * @param folder le dossier cible
     * @return la liste des P2PFile créée à partir des fichiers trouvés dans le
     * dossier
     */
    public static ArrayList<P2PFile> getLocalFiles(File folder) {
        ArrayList<P2PFile> fileList = null;

        for (final File fileEntry : folder.listFiles()) {
            // On n'affiche pas les dossiers
            if (!fileEntry.isDirectory()) {
                if (fileList == null) {
                    fileList = new ArrayList<>();
                }
                // Pour chaque fichier, on créé un P2PFile avec son nom et sa taille et on l'ajoute a la liste.
                // TODO : Possiblité d'optimisation : deux new file folder.getAbsolutePath() + "\\" +
                fileList.add(new P2PFile(fileEntry.getName(), fileEntry.length()));
                System.out.println("DEBUG : (localfile) " + folder.getAbsolutePath() + "\\" + fileEntry.getName());
            }
        }

        return fileList;
    }

    /**
     * Permet de passer un set de P2PFile en ArrayList.
     *
     * @param keySet le set de P2PFile
     * @return la liste de P2PFile
     */
    public static ArrayList<P2PFile> setToArrayList(Set<P2PFile> keySet) {
        Iterator<P2PFile> it = keySet.iterator();
        ArrayList<P2PFile> fileList = new ArrayList();
        
        while (it.hasNext()) {
            fileList.add(it.next());
        }
        if(fileList.isEmpty()) {
            return null;
        }
        
        return fileList;
    }
}
