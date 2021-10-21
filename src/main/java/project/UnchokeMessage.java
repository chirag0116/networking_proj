package project;

public class UnchokeMessage extends Message {

    private static final byte TYPE = 1;

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
    protected String getTypeBytes() {
        byte[] bytes = {TYPE};
        return new String(bytes);
    }

    @Override
    protected String getPayloadBytes() {
        return ""; // No payload
    }

    @Override
    protected int getLength() {
        return 5; // 4 length bytes + 1 type byte
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        else if (obj.getClass() != this.getClass()) {
            return false;
        }

        final UnchokeMessage other = (UnchokeMessage) obj;
        // Length and Type are equal since types are equal
        return this.peer.equals(other.getPeer());

    }
}
