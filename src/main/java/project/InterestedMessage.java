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

}
