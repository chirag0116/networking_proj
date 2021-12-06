package project;

import java.util.logging.Logger;

public class MessageLogger {

    private Logger log;

    public MessageLogger(int id) {
        this.log = Logger.getLogger("log_peer_" + id);
    }

}

