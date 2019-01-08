/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
 */
package cli;

import comServCli.P2PFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Classe permettant l'écriture d'un fichier en réordonnant ses morceaux.
 * 
 * @author Axel Couturier
 */
public class ConcurrentFileStream {

    private P2PFile fichier;
    private RandomAccessFile stream;

    /**
     * Constructeur de ConcurrentFileStream.
     * 
     * @param folder le répertoire cible
     * @param fichier le P2PFile représentant le fichier à écrire
     */
    public ConcurrentFileStream(File folder, P2PFile fichier) {
        this.fichier = fichier;
        
        try {
            stream = new RandomAccessFile(folder.getAbsolutePath() + "\\" + fichier.getFilename(), "rw");
            stream.seek(0);
            stream.write(new byte[(int)fichier.getSize()]);
        } catch (FileNotFoundException e) {
            System.out.println(e);
            System.exit(1);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Permet l'écriture à partir de la position <filePointerOffset> des données contenues dans le buffer.
     * 
     * @param filePointerOffset la position de départ
     * @param bytes le buffer de données
     */
    public synchronized void write(long filePointerOffset, byte[] bytes) {
        try {
            //Sets the file-pointer offset, measured from the beginning of this file, at which the next read or write occurs.
            if (stream.length() < filePointerOffset) {
                stream.seek(stream.length() - 1);
                stream.write(bytes);
            } else {
                stream.seek(filePointerOffset);
                stream.write(bytes);
            }
            
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Ferme le flux vers le fichier.
     */
    public void close() {
        try {
            stream.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    /**
     * Renvoie la taille du fichier ou -1 en cas d'erreur.
     * 
     * @return la taille du fichier
     */
    public long getSize() {
        try {
            return stream.length();
        } catch (IOException e) {
            System.out.println(e);
        }
        return -1;
    }

    public P2PFile getFichier() {
        return fichier;
    }
}
