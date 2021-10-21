package project;

import java.nio.ByteBuffer;

public class RequestMessage extends Message {

    private int index;

    /**
     * Construct a ChokeMessage with a specified
     * sender. Should be used when receiving messages.
     * @param index - index of a file piece; see specification
     * @param peer - The sender of the message (accessibly
     *               by client).
     */
    public RequestMessage(int index, PeerConfiguration peer) {
        this.index = index;
        this.peer = peer;
    }

    public int getIndex() {
        return index;
    }

    @Override
    protected byte getType() {
        return 6;
    }

    @Override
    protected String getPayloadBytes() {
        byte[] bytes = ByteBuffer.allocate(4).putInt(index).array();
        return new String(bytes);
    }

    @Override
    protected int getLength() {
        return 9; // 4 length bytes + 1 type byte + 4 index bytes
    }
}
