/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */ 

package comServCli;

/**
 *
 * @author Axel Couturier
 */
public class P2PParam {

    /**
     * Taille maximum des buffers des DatagramPacket permettant l'envoi des fichiers sous forme d'octets.
     */
    public static final int TAILLE_BUF = 1024;

    /**
     * Temps d'attente maximum après envoi d'un DatagramPacket.
     */
    public static final int TIMEOUT_UDP = 30;

}
