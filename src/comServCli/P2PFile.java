/*
 * LPRO 2018/2019
 * Université de Franche-Comté
 * Projet réalisé par Axel Couturier et Axel Ledermann.
 */
package comServCli;

import java.io.Serializable;
import java.util.Objects;

/**
 * Classe représentant un fichier par son nom et sa taille.
 * 
 * @author Axel Couturier
 */
public class P2PFile implements Serializable {

    private String filename;
    private long size;

    /**
     * Constructeur de P2PFile.
     * 
     * @param filename String représentant le nom du fichier
     * @param size la taille du fichier
     */
    public P2PFile(String filename, long size) {
        this.filename = filename;
        this.size = size;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.filename);
        hash = 83 * hash + (int) (this.size ^ (this.size >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final P2PFile other = (P2PFile) obj;
        if (this.size != other.size) {
            return false;
        }
        if (!Objects.equals(this.filename, other.filename)) {
            return false;
        }
        return true;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return filename + " [Size=" + size + ']';
    }
}
