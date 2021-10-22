package project;

import sun.text.bidi.BidiLine;

public class PeerInfo {
    private String id;
    private String ip;
    private int port;

    /**
     * Creates a PeerInfo object
     *
     * @param id is the peer's unique id
     * @param ip is the peer's IP address
     * @param port is the port (probably TCP)
     */
    public PeerInfo(String id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    /**
     * Creates and intializes a PeerInfo object using IP address and port.
     * Sets @param id to the combination
     *
     * @param ip is the peer's IP address
     * @param port is the port number
     */
    public PeerInfo(String ip, int port) {
        this(ip + ":" + port, ip, port);
    }

    /**
     * Same thing^ just w/ port
     *
     * @param port is the peer's port number
     */
    public PeerInfo(int port) {
        this(null, port);
    }

    /***
     * @return the string ID
     */
    public String getId() {
        return id;
    }

    /**
     * @return the IP address
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * @param id set to a new id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param ip set to a new IP address
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @param port set to a new port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the full identifier and address of the peer
     */
    public String getFull() {
        return id + "(" + ip + ":" + port + ")";
    }
}
