package comServCli;

import java.util.ArrayList;

/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */
/**
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
}
