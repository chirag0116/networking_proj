package project;

import java.nio.ByteBuffer;

public class PieceMessage extends Message {

    private int index;
    private byte[] piece;

    /**
     * Construct a ChokeMessage with a specified
     * sender. Should be used when receiving messages.
     * @param index - index of a file piece; see specification
     * @param peer - The sender of the message (accessibly
     *               by client).
     */
    public PieceMessage(int index, byte[] piece, PeerConfiguration peer) {
        this.index = index;
        this.piece = piece;
        this.peer = peer;
    }

    public int getIndex() {
        return index;
    }

    public byte[] getPiece() {
        return piece;
    }

    @Override
    protected byte getType() {
        return 7;
    }

    @Override
    protected String getPayloadBytes() {
        byte[] indexBytes = ByteBuffer.allocate(4).putInt(index).array();
        byte[] bytes = new byte[indexBytes.length+piece.length];
        System.arraycopy(indexBytes, 0, bytes, 0, indexBytes.length);
        System.arraycopy(piece, 0, bytes, 4, piece.length);
        return new String(bytes);
    }

    @Override
    protected int getLength() {
        return 9 + piece.length; // 4 length bytes + 1 type byte + 4 index bytes + N piece bytes
    }
}
