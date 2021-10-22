package project;

import java.util.List;

public class BitfieldMessage extends Message {

    /*
     * The serialized BitfieldMessage has the
     * ownership status encoded *within* bytes.
     * ith bit == 1 -> the sender has the ith piece
     */
    byte[] bitfield;

    /**
     * Constructor which accepts a boolean array
     * which is encoded into the bitfield (byte array).
     * Used for construction by Peer class when creating
     * bitfield messages for sending to server.
     * @param bitfield - boolean array which is encoded into bitfield
     * @param peer - the sender or intended receiver (context dependent)
     */
    BitfieldMessage(boolean[] bitfield, PeerConfiguration peer) {
        this.peer = peer;
        this.bitfield = bitfieldFromBooleanArray(bitfield);
    }

    /**
     * Constructor which accepts bitfield
     * (byte array) directly. Used by MessageFactory
     * for constructing BitfieldMessage objects from raw
     * bytes off TCP
     * @param bitfield - byte array which is the bitfield
     * @param peer- the sender or intended receiver (context dependent)
     */
    BitfieldMessage(byte[] bitfield, PeerConfiguration peer) {
        this.peer = peer;
        this.bitfield = bitfield;
    }

    boolean hasPiece(int index) {
        if (index > bitfield.length * 8 || index < 0) {
            throw new IndexOutOfBoundsException("Invalid bit index in bitfield");
        }
        int arrIndex = index / 8;
        int bitIndex = index % 8;
        byte b = bitfield[arrIndex];
        // This LHS will be all zeroes only when the
        // specified bit is 1
        return ~(b | ~(1 << bitIndex)) == 0;
    }

    @Override
    protected byte getType() {
        return 5;
    }

    @Override
    protected String getPayloadBytes() {
        return StringEncoder.bytesToString(bitfield);
    }

    /**
     * Encodes a boolean array into bytes
     * such that:
     * arr[i] == true -> ith bit in byte[] == 1
     * The returned byte[] will be padded with
     * extra bits to contain an integer number of bytes
     * @param arr - array of booleans
     * @return array of bytes where the ith bit is 1 if arr[i] is 1
     */
    private byte[] bitfieldFromBooleanArray(boolean[] arr) {
        int neededBytes = (arr.length % 8 == 0) ? arr.length/8 : arr.length/8 + 1;
        byte[] bytes = new byte[neededBytes];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i]) {
                int arrayIdx = i / 8;
                int bitIdx = i % 8;
                byte modified = setBit(bytes[arrayIdx], (byte)bitIdx);
                bytes[arrayIdx] = modified;
            }
        }
        return bytes;
    }

    /**
     * Sets the ith bit to 1
     * @param b - original byte
     * @param i - index of the bit to be set (0-7)
     * @return b with the ith bit set to 1
     */
    private byte setBit(byte b, byte i) {
        if (i < 0 || i > 7) {
            throw new IllegalArgumentException("Invalid byte index");
        }
        return (byte) (b | (1 << i));
    }

    @Override
    protected int getLength() {
        return 5 + bitfield.length;
    }
}
