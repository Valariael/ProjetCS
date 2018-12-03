/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */
package comServCli;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Axel Couturier
 */
public class P2PFile {

    private String filename;
    private long size;

    public P2PFile(String filename) {
        this.filename = filename;
        updateSize();
    }

    public void updateSize() {
        File fichier = new File(filename);
        if (fichier.exists() && !fichier.isDirectory()) {
            size = fichier.length();
        }
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
    
    
}
