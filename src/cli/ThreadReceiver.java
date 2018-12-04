/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */ 

package cli;

import java.net.DatagramSocket;

/**
 *
 * @author Axel Couturier
 */
public class ThreadReceiver extends Thread {
    private DatagramSocket sockUDPReceive;
    private ConcurrentFileStream concurentfilestream;
    
    
    
    
    public void run() {
        // Recoit les paquets et écrit dans le fichier de destination
        // Utiliser la classe concurentfilestream;
        sockUDPReceive.receive(pkRequete);
    }
    
}
