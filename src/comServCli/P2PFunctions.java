package comServCli;

import java.io.File;
import java.util.ArrayList;

/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */
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
        if (listeFichiers.size() >= 1) {
            for (P2PFile fichier : listeFichiers) {
                i++;
                System.out.println("\t " + (numerotee ? i + ". " : "- ") + fichier.toString());
            }
        } else {
            System.out.println("Il n'y a aucun résultat");
        }

    }
    
    /**
     * Recherche tous les fichiers présents dans le dossier passé en paramètre.
     * 
     * @param folder le dossier cible
     * @return la liste des P2PFile créée à partir des fichiers trouvés dans le dossier
     */
    public static ArrayList<P2PFile> getLocalFiles(File folder) {
        ArrayList<P2PFile> fileList = null;
        
        for (final File fileEntry : folder.listFiles()) {
            // On n'affiche pas les dossiers
            if (!fileEntry.isDirectory()) {
                if(fileList == null) fileList = new ArrayList<>();
                // Pour chaque fichier, on créé un P2PFile avec son nom et sa taille et on l'ajoute a la liste.
                // TODO : Possiblité d'optimisation : deux new file
                fileList.add(new P2PFile(folder.getAbsolutePath() + "\\" + fileEntry.getName(), fileEntry.length()));
                System.out.println("DEBUG : (localfile) " + folder.getAbsolutePath() + "\\" + fileEntry.getName());
            }
        }
        // TODO: possibilité d'évolution = recherche récursive des fichiers
        
        return fileList;
    }
}
