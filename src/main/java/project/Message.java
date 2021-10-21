package project;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * Abstract Base Class of "actual messages"
 * from specification. Subclasses implement particular
 * message types and abstract methods.
 */
public abstract class Message {

    // Peer who this message was sent from, or
    // received from
    protected PeerConfiguration peer;

    /**
     * Returns the string capturing the bytes
     * of the message; i.e. serializes the message
     * @return String capturing message bytes
     */
    public String serialize() {
        return getLengthBytes() + getTypeBytes() + getPayloadBytes();
    }

    protected String getLengthBytes() {
        byte[] bytes = ByteBuffer.allocate(4).putInt(getLength()).array();
        return new String(bytes);
    }

    protected String getTypeBytes() {
        byte[] bytes = {getType()};
        return new String(bytes);
    }

    /*
     * Abstract methods used by Message::serialize
     */

    protected abstract String getPayloadBytes();

    /**
     * Computes and returns the length of
     * this message once serialized.
     * @return length of the message, once serialized
     */
    protected abstract int getLength();

    /**
     * Returns the byte value of the
     * message type (constant in sub-class)
     * @return type as a byte
     */
    protected abstract byte getType();

    /**
     * Returns the peer member,
     * which is either the sender or
     * receiver of this message
     * @return peer for this message
     */
    public PeerConfiguration getPeer() {
        return peer;
    }

}
