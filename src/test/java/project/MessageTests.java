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

        MessageFactory factory = new MessageFactory();
        Message expectedMsg = factory.makeMessage(received, PEER1);
        Assertions.assertEquals(msg,expectedMsg);
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

        MessageFactory factory = new MessageFactory();
        Message expectedMsg = factory.makeMessage(received, PEER1);
        Assertions.assertEquals(msg,expectedMsg);
    }

    @Test
    void testInterestedMessageEquals() {
        InterestedMessage m1 = new InterestedMessage(PEER1);
        InterestedMessage m2 = new InterestedMessage(PEER1);
        InterestedMessage m3 = new InterestedMessage(PEER2);
        Assertions.assertEquals(m1,m2);
        Assertions.assertNotEquals(m1,m3);
    }

    @Test
    void testMessageFactoryInterestedMessage() {
        byte[] bytes = {0,0,0,5,2};
        Message received = messageFromBytes(bytes, PEER1);
        InterestedMessage expected = new InterestedMessage(PEER1);
        Assertions.assertTrue(received instanceof InterestedMessage);
        Assertions.assertEquals(expected, received);
    }

    @Test
    void testInterestedMessageSerialization() {
        byte[] bytes = {0,0,0,5,2};
        String expected = new String(bytes);
        InterestedMessage msg = new InterestedMessage(PEER1);
        String received = msg.serialize();
        Assertions.assertEquals(expected,received);

        MessageFactory factory = new MessageFactory();
        Message expectedMsg = factory.makeMessage(received, PEER1);
        Assertions.assertEquals(msg,expectedMsg);
    }

    @Test
    void testUninterestedMessageEquals() {
        UninterestedMessage m1 = new UninterestedMessage(PEER1);
        UninterestedMessage m2 = new UninterestedMessage(PEER1);
        UninterestedMessage m3 = new UninterestedMessage(PEER2);
        Assertions.assertEquals(m1,m2);
        Assertions.assertNotEquals(m1,m3);
    }

    @Test
    void testMessageFactoryUninterestedMessage() {
        byte[] bytes = {0,0,0,5,3};
        Message received = messageFromBytes(bytes, PEER1);
        UninterestedMessage expected = new UninterestedMessage(PEER1);
        Assertions.assertTrue(received instanceof UninterestedMessage);
        Assertions.assertEquals(expected, received);
    }

    @Test
    void testUninterestedMessageSerialization() {
        byte[] bytes = {0,0,0,5,3};
        String expected = new String(bytes);
        UninterestedMessage msg = new UninterestedMessage(PEER1);
        String received = msg.serialize();
        Assertions.assertEquals(expected,received);

        MessageFactory factory = new MessageFactory();
        Message expectedMsg = factory.makeMessage(received, PEER1);
        Assertions.assertEquals(msg,expectedMsg);
    }

    @Test
    void testHaveMessageEquals() {
        HaveMessage m1 = new HaveMessage(1, PEER1);
        HaveMessage m2 = new HaveMessage(1, PEER1);
        HaveMessage m3 = new HaveMessage(1, PEER2);
        HaveMessage m4 = new HaveMessage(2, PEER1);
        Assertions.assertEquals(m1,m2);
        Assertions.assertNotEquals(m1,m3);
        Assertions.assertNotEquals(m1,m4);
        Assertions.assertNotEquals(m3,m4);
    }

    @Test
    void testMessageFactoryHaveMessage() {
        byte[] bytes = {0,0,0,9,4,0,0,0,1};
        Message received = messageFromBytes(bytes, PEER1);
        HaveMessage expected = new HaveMessage(1, PEER1);
        Assertions.assertTrue(received instanceof HaveMessage);
        Assertions.assertEquals(expected, received);
    }

    @Test
    void testHaveMessageSerialization() {
        byte[] bytes = {0,0,0,9,4,0,0,0,1};
        String expected = new String(bytes);
        HaveMessage msg = new HaveMessage(1, PEER1);
        String received = msg.serialize();
        Assertions.assertEquals(expected,received);

        MessageFactory factory = new MessageFactory();
        Message expectedMsg = factory.makeMessage(received, PEER1);
        Assertions.assertEquals(msg,expectedMsg);
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
