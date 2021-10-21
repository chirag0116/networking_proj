package project;

public class ChokeMessage extends Message {

    /**
     * Construct a ChokeMessage with a specified
     * sender. Should be used when receiving messages.
     * @param peer - The sender of the message (accessibly
     *               by client).
     */
    public ChokeMessage(PeerConfiguration peer) {
        this.peer = peer;
    }

    @Override
    protected byte getType() {
        return 0;
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

        final ChokeMessage other = (ChokeMessage) obj;
        // Length and Type are equal since types are equal
        return this.peer.equals(other.getPeer());

    }
}
