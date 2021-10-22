package project;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageFactory {

    public Message makeMessage(String rawMsg, PeerConfiguration peer) {
        // TODO - Verify this is the correct charset with tests
        List<Byte> bytes = getByteList(rawMsg);

        int length = intFromBytes(bytes.subList(0,4));
        if (rawMsg.length() != length) {
            throw new IllegalArgumentException("Length field does not match string received by MessageFactory");
        }

        byte type = bytes.get(4);
        List<Byte> payload = Arrays.asList();
        if (bytes.size() > 5) {
            payload = bytes.subList(5,bytes.size());
        }

        Message msg = null;
        switch(type) {
            case 0:
                msg = new ChokeMessage(peer);
                break;
            case 1:
                msg = new UnchokeMessage(peer);
                break;
            case 2:
                msg = new InterestedMessage(peer);
                break;
            case 3:
                msg = new UninterestedMessage(peer);
                break;
            case 4:
                if (payload.size() != 4) {
                    throw new IllegalArgumentException("Invalid payload size for HaveMessage");
                }
                else {
                    int index = intFromBytes(payload);
                    msg = new HaveMessage(index, peer);
                }
                break;
            case 5:
                if (payload.isEmpty()) {
                    throw new IllegalArgumentException("Empty payload in BitfieldMessage");
                }
                else {
                    byte[] bitfield = new byte[payload.size()];
                    for (int i = 0; i < payload.size(); i++) {
                        bitfield[i] = payload.get(i);
                    }
                    msg = new BitfieldMessage(bitfield, peer);
                }
                break;
            case 6:
                if (payload.size() != 4) {
                    throw new IllegalArgumentException("Invalid payload size for RequestMessage");
                }
                else {
                    int index = intFromBytes(payload);
                    msg = new RequestMessage(index, peer);
                }
                break;
            case 7:
                if (payload.size() < 4) {
                    throw new IllegalArgumentException("Invalid payload size for PieceMessage");
                }
                else {
                    int index = intFromBytes(payload.subList(0,4));
                    List<Byte> pieceList = payload.subList(4, payload.size());
                    byte[] piece = new byte[pieceList.size()];
                    for (int i = 0; i < pieceList.size(); i++) {
                        piece[i] = pieceList.get(i);
                    }
                    msg = new PieceMessage(index, piece, peer);
                }
                break;
            default:
                throw new IllegalArgumentException("Unexpected message type in raw message");
        }
        return msg;
    }

    private List<Byte> getByteList(String s) {
        byte[] byteArray = StringEncoder.stringToBytes(s);
        List<Byte> bytes = new ArrayList<>();
        for (byte b : byteArray) {
            bytes.add(b);
        }
        return bytes;
    }

    /**
     * Computes length (int) from list of 4 bytes
     * @param byteList - 4 byte list capturing bytes of the int
     * @return int represented by bytes
     */
    private int intFromBytes(List<Byte> byteList) {
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            bytes[i] = byteList.get(i);
        }
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return buf.getInt();
    }

}
