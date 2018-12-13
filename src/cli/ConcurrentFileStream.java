/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */
package cli;

import comServCli.P2PFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Axel Couturier
 */
public class ConcurrentFileStream {

    private P2PFile fichier;
    private File folder;
    private RandomAccessFile stream;

    public ConcurrentFileStream(File folder, P2PFile fichier) {
        this.fichier = fichier;
        this.folder = folder;
        try {
            stream = new RandomAccessFile(folder.getAbsolutePath() + "\\" + fichier.getFilename(), "rw");
            stream.seek(0);
            stream.write(new byte[(int)fichier.getSize()]);
        } catch (FileNotFoundException e) {
            System.out.println(e);
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(ConcurrentFileStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        // TODO : Reset content of file ?
    }

    public synchronized void write(long filePointerOffset, byte[] bytes) {
        System.out.println("DEBUG : ConcurrentFileStream : Fichier en cours d'édition " + fichier.getFilename() + " a la position " + filePointerOffset);

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

    public void close() {
        try {
            stream.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public long getSize() {
        try {
            return stream.length();
        } catch (IOException ex) {
            Logger.getLogger(ConcurrentFileStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    public P2PFile getFichier() {
        return fichier;
    }
    
    

}
