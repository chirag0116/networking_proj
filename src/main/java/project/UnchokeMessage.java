package project;

public class UnchokeMessage extends Message {

    /**
     * Construct an UnchokeMessage with a specified
     * sender. Should be used when receiving messages.
     * @param peer - The sender of the message (accessibly
     *               by client).
     */
    public UnchokeMessage(PeerConfiguration peer) {
        this.peer = peer;
    }

    @Override
    protected byte getType() {
        return 1;
    }

    @Override
    protected String getPayloadBytes() {
        return ""; // No payload
    }

    @Override
    protected int getLength() {
        return 5; // 4 length bytes + 1 type byte
    }

}
