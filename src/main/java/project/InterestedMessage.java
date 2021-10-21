package project;

public class InterestedMessage extends Message {

    public InterestedMessage(PeerConfiguration peer) {
        this.peer = peer;
    }

    @Override
    protected byte getType() {
        return 2;
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

        final InterestedMessage other = (InterestedMessage) obj;
        // Length and Type are equal since types are equal
        return this.peer.equals(other.getPeer());

    }
}
