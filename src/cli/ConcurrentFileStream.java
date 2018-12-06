/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */
package cli;

import comServCli.P2PFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Axel Couturier
 */
public class ConcurrentFileStream { // extends P2PFile ?

    private P2PFile fichier;

    public ConcurrentFileStream(P2PFile fichier) {
        this.fichier = fichier;
    }
    
    

    public synchronized void write(int filePointerOffset, byte[] bytes) {
        RandomAccessFile stream = null;
        try {
            stream = new RandomAccessFile(fichier.getFilename(), "rw");
        } catch (FileNotFoundException e) {
            System.out.println(e);
            System.exit(1);
        }

        try {
            //Sets the file-pointer offset, measured from the beginning of this file, at which the next read or write occurs.
            stream.seek(filePointerOffset);
            stream.write(bytes);
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

}
