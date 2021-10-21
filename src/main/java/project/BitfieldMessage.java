package project;

import java.util.List;

public class BitfieldMessage extends Message {

    /*
     * The serialized BitfieldMessage has the
     * ownership status encoded *within* bytes.
     * Instead of storing the in-byte encoding,
     * the BitFieldMessage class stores am equivalent
     * boolean array.
     * The array is as follows:
     * bitfield[i] == true -> the sender has the ith piece
     */
    boolean[] bitfield;

    BitfieldMessage(boolean[] bitfield, PeerConfiguration peer) {
        this.peer = peer;
        this.bitfield = bitfield;
    }

    boolean hasPiece(int index) {
        return bitfield[index];
    }

    boolean[] getBitfield() {
        return bitfield;
    }

    @Override
    protected byte getType() {
        return 5;
    }

    @Override
    protected String getPayloadBytes() {
        int neededBytes = (bitfield.length % 8 == 0) ? bitfield.length/8 : bitfield.length/8 + 1;
        byte[] bytes = new byte[neededBytes];
        for (int i = 0; i < bitfield.length; i++) {
            if (bitfield[i]) {
                int arrayIdx = i / 8;
                int bitIdx =  i % 8;
                byte modified = setBit(bytes[arrayIdx], bitIdx);
                bytes[arrayIdx] = modified;
            }
        }
        return new String(bytes);
    }

    /**
     * Sets the ith bit to 1
     * @param b - original byte
     * @param i - index of the bit to be set (0-7)
     * @return b with the ith bit set to 1
     */
    private byte setBit(byte b, int i) {
        if (i < 0 || i > 7) {
            throw new IllegalArgumentException("Invalid byte index");
        }
        return (byte) (b | (1 << i));
    }

    @Override
    protected int getLength() {
        // 4 length bytes + 1 type byte + X bitfield bytes
        // X = (# of pieces)/8 if # of pieces fits neatly into bytes
        // X = (# of pieces)/8 + 1 if there are trailing extra bits
        int numPieces = bitfield.length;
        if (numPieces % 8 == 0) {
            return 5 + numPieces/8;
        }
        else {
            return 5 + numPieces/8 + 1; // Round up
        }
    }
}
