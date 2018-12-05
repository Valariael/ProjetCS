/* LPRO 2018/2019
0  To change this license header, choose License Headers in Project Properties.
0  To change this template file, choose Tools | Templates
0  and open the template in the editor.
0 */ 

package comServCli;

import java.util.Objects;

/**
 * Représente l'adresse d'un serveur avec l'adresse IP de l'hôte et son numéro de port.
 * 
 * @author Axel Couturier
 */
public class AddressServer {
    private final String host;
    private final int port;

    /**
     * Constructeur de la classe AddressServer.
     * 
     * @param host l'adresse IP¨de l'hôte sous forme de String
     * @param port le numéro de port de l'hôte en int
     */
    public AddressServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Retourne une String représentant l'adresse IP de l'hôte.
     * @return l'adresse IP de l'hôte
     */
    public String getHost() {
        return host;
    }

    /**
     * Retourne l'int représentant le numéro de port de l'hôte.
     * @return le numéro de port de l'hôte
     */
    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.host);
        hash = 71 * hash + this.port;
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
        final AddressServer other = (AddressServer) obj;
        if (this.port != other.port) {
            return false;
        }
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        return true;
    }
    
     
    
    
}
