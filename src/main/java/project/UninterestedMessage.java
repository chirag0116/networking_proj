package project;

public class UninterestedMessage extends Message {

    public UninterestedMessage(PeerConfiguration peer) {
        this.peer = peer;
    }

    @Override
    protected byte getType() {
        return 3;
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
