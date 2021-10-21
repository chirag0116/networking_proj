package project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MessageTests {

    private static final PeerConfiguration PEER1 = new PeerConfiguration(1011, "lin114-00.cise.ufl.edu",6008,false);
    private static final PeerConfiguration PEER2 = new PeerConfiguration(1012, "lin114-01.cise.ufl.edu",6008,false);

    private Message messageFromBytes(byte[] bytes, PeerConfiguration peer) {
        String raw = new String(bytes);
        MessageFactory factory = new MessageFactory();
        return factory.makeMessage(raw, peer);
    }

    @Test
    void testChokeMessageEquals() {
        ChokeMessage m1 = new ChokeMessage(PEER1);
        ChokeMessage m2 = new ChokeMessage(PEER1);
        ChokeMessage m3 = new ChokeMessage(PEER2);
        Assertions.assertEquals(m1,m2);
        Assertions.assertNotEquals(m1,m3);
    }

    @Test
    void testMessageFactoryChokeMessage() {
        byte[] bytes = {0,0,0,5,0};
        Message received = messageFromBytes(bytes, PEER1);
        ChokeMessage expected = new ChokeMessage(PEER1);
        Assertions.assertTrue(received instanceof ChokeMessage);
        Assertions.assertEquals(expected, received);
    }

    @Test
    void testChokeMessageSerialization() {
        byte[] bytes = {0,0,0,5,0};
        String expected = new String(bytes);
        ChokeMessage msg = new ChokeMessage(PEER1);
        String received = msg.serialize();
        Assertions.assertEquals(expected,received);
    }

    @Test
    void testUnchokeMessageEquals() {
        UnchokeMessage m1 = new UnchokeMessage(PEER1);
        UnchokeMessage m2 = new UnchokeMessage(PEER1);
        UnchokeMessage m3 = new UnchokeMessage(PEER2);
        Assertions.assertEquals(m1,m2);
        Assertions.assertNotEquals(m1,m3);
    }

    @Test
    void testMessageFactoryUnchokeMessage() {
        byte[] bytes = {0,0,0,5,1};
        Message received = messageFromBytes(bytes, PEER1);
        UnchokeMessage expected = new UnchokeMessage(PEER1);
        Assertions.assertTrue(received instanceof UnchokeMessage);
        Assertions.assertEquals(expected, received);
    }

    @Test
    void testUnchokeMessageSerialization() {
        byte[] bytes = {0,0,0,5,1};
        String expected = new String(bytes);
        UnchokeMessage msg = new UnchokeMessage(PEER1);
        String received = msg.serialize();
        Assertions.assertEquals(expected,received);
    }

    @Test
    void testMessageFactoryInvalidType() {
        byte[] bytes = {0,0,0,5,12};
        String raw = new String(bytes);
        MessageFactory factory = new MessageFactory();
        Assertions.assertThrows(IllegalArgumentException.class, () -> factory.makeMessage(raw, PEER1));
    }
    @Test
    void testMessageFactoryInvalidLength() {
        byte[] bytes = {0,0,0,12,0};
        String raw = new String(bytes);
        MessageFactory factory = new MessageFactory();
        Assertions.assertThrows(IllegalArgumentException.class, () -> factory.makeMessage(raw, PEER1));
    }

}
