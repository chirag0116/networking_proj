package project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MessageTests {

    private static final PeerConfiguration PEER = new PeerConfiguration(1011, "lin114-00.cise.ufl.edu",6008,false);

    @Test
    void testChokeMessageEquals() {
        ChokeMessage m1 = new ChokeMessage(PEER);
        ChokeMessage m2 = new ChokeMessage(PEER);
        Assertions.assertEquals(m1,m2);
    }

    @Test
    void testMessageFactoryChokeMessage() {
        byte[] bytes = {0,0,0,5,0};
        String raw = new String(bytes);
        MessageFactory factory = new MessageFactory();
        Message received = factory.makeMessage(raw, PEER);
        ChokeMessage expected = new ChokeMessage(PEER);

        Assertions.assertTrue(received instanceof ChokeMessage);
        Assertions.assertEquals(expected, received);
    }

    @Test
    void testChokeMessageSerialization() {
        byte[] bytes = {0,0,0,5,0};
        String expected = new String(bytes);
        ChokeMessage msg = new ChokeMessage(PEER);
        String received = msg.serialize();
        Assertions.assertEquals(expected,received);
    }

    @Test
    void testMessageFactoryInvalidType() {
        byte[] bytes = {0,0,0,5,12};
        String raw = new String(bytes);
        MessageFactory factory = new MessageFactory();
        Assertions.assertThrows(IllegalArgumentException.class, () -> factory.makeMessage(raw, PEER));
    }
    @Test
    void testMessageFactoryInvalidLength() {
        byte[] bytes = {0,0,0,12,0};
        String raw = new String(bytes);
        MessageFactory factory = new MessageFactory();
        Assertions.assertThrows(IllegalArgumentException.class, () -> factory.makeMessage(raw, PEER));
    }

}
