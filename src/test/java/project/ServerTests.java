package project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class ServerTests {

    private static final PeerConfiguration PEER1 = new PeerConfiguration(1011, "lin114-00.cise.ufl.edu",6008,false);
    private static final PeerConfiguration PEER2 = new PeerConfiguration(1012, "lin114-01.cise.ufl.edu",6008,false);

    /**
     * Client stub which is used when Server
     * does a passive connect. Tries to open
     * a connection to address:port
     * and then waits for messages.
     */
    private static class ClientStub extends Thread {
        public InetAddress address;
        public int port;
        public Queue<String> messages;

        public ClientStub(InetAddress address, int port) {
            this.address = address;
            this.port = port;
            this.messages = new LinkedList<>();
        }

        @Override
        public void run() {
            Socket conn;
            try {
                conn = new Socket(address, port);
            }
            catch (IOException e) {
                reportException(e);
                System.out.println("ClientStub: Connection could not be opened");
                return;
            }

            ObjectInputStream in;
            try {
                in = new ObjectInputStream(conn.getInputStream());
            }
            catch (IOException e) {
                reportException(e);
                return;
            }

            while (!this.isInterrupted()) {
                try {
                    messages.add((String)in.readObject());
                }
                catch (Exception e) {
                    reportException(e);
                    return;
                }
            }
        }

        public void reportException(Exception e) {
            System.out.println("ClientStub Exception: " + e);
        }
    }

    /**
     * Server stub which is used to simulate another
     * peer when a Server object does an active connect.
     * Listens on the provided port for a connection,
     * and then waits for messages once one is established.
     */
    private static class ServerStub extends Thread {
        public int port;
        public Queue<String> messages;

        public ServerStub(int port) {
            this.port = port;
            this.messages = new LinkedList<>();
        }

        @Override
        public void run() {
            Socket conn;
            try {
                ServerSocket listener = new ServerSocket(port);
                conn = listener.accept();
            }
            catch (IOException e) {
                reportException(e);
                System.out.println("ServerStub: Connection could not be opened");
                return; // Terminate and kill thread
            }

            ObjectInputStream in;
            try {
                in = new ObjectInputStream(conn.getInputStream());
            }
            catch (IOException e) {
                reportException(e);
                return;
            }

            while (!this.isInterrupted()) {
                try {
                    messages.add((String)in.readObject());
                }
                catch (Exception e) {
                    reportException(e);
                    return;
                }
            }
        }

        public void reportException(Exception e) {
            System.out.println("ServerStub Exception: " + e);
        }
    }

    private InetAddress getLocalHost() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            Assertions.fail();
        }
        return address;
    }

    @Test
    void testMakeHandshakeMessage() {
        Server server = new Server(PEER1, PEER2, false, (Message m) -> {});

        String msg = server.makeHandshakeMessage();

        Assertions.assertEquals(msg.substring(0,18), "P2PFILESHARINGPROJ");
        byte[] zeros =  {0,0,0,0,0,0,0,0,0,0};
        Assertions.assertArrayEquals(StringEncoder.stringToBytes(msg.substring(18,28)), zeros);
        byte[] idBytes = {0,0,0,0};
        System.arraycopy(StringEncoder.stringToBytes(msg), 28, idBytes, 0, 4);
        Assertions.assertEquals(ByteBuffer.wrap(idBytes).getInt(), PEER1.getId());
    }

    @Test
    void testValidateHandshakeMessage() {
        Server server1 = new Server(PEER1, PEER2, false, (Message m) -> {});
        Server server2 = new Server(PEER2, PEER1, false, (Message m) -> {});
        String msg = server1.makeHandshakeMessage();

        // makeHandshakeMessage is correct by previous test, so
        // only need to test validation
        Assertions.assertTrue(server2.validateHandshake(msg));
    }

    @Test
    void testPassiveConnectSuccessful() {
        int port = 8000;
        InetAddress address = getLocalHost();

        ClientStub client = new ClientStub(address, port);
        client.start();
        Socket conn = Server.passiveConnect(port);

        Assertions.assertNotNull(conn);
        Assertions.assertTrue(conn.isConnected());
        client.interrupt();
        try {
            conn.close();
        }
        catch (IOException e) {
            Assertions.fail();
        }
    }

    @Test
    void testActiveConnectSuccessful() {
        int port = 8000;
        InetAddress address = getLocalHost();

        ServerStub serverstub = new ServerStub(port);
        serverstub.start();
        Socket conn = Server.activeConnect(address, port);

        Assertions.assertNotNull(conn);
        Assertions.assertTrue(conn.isConnected());
        try {
            conn.close();
        }
        catch (IOException e) {
            Assertions.fail();
        }
    }

    @Test
    void testActiveConnectFailure() {
        int port = 8000;
        InetAddress address = getLocalHost();
        Socket conn = Server.activeConnect(address, port);

        Assertions.assertNull(conn);
    }

    @Test
    void testSendSingleMessage() {
        int port = 8000;
        PeerConfiguration targetConfiguration = new PeerConfiguration(1001, "localhost", port, false);
        PeerConfiguration selfConfiguration = new PeerConfiguration(1002, "localhost", port + 1, false);
        HaveMessage msg = new HaveMessage(1, targetConfiguration);
        System.out.printf("Expected message info: %s%n", msg.info());

        AtomicReference<Message> targetReceived = new AtomicReference<>();
        AtomicReference<Boolean> receiveDone = new AtomicReference<>(false);
        final ServerTests instance = this;

        Server target = new Server(targetConfiguration, selfConfiguration, true, (Message m) -> {
            System.out.printf("Expected message info: %s%n", m.info());
            targetReceived.set(m);
            receiveDone.set(true);
            synchronized (instance) {
                instance.notify();
            }
        });

        Server server = new Server(selfConfiguration /* dummy */, targetConfiguration, false, (Message m) -> {});

        Thread t = new Thread(() -> {
            Assertions.assertTrue(server.start());
            try {
                Assertions.assertTrue(server.sendMessage(msg));
            }
            catch (Exception e) {
                Assertions.fail(e.toString());
            }
        });
        t.start();

        Assertions.assertTrue(target.start());

        // Wait till the message is received to check equality
        while (!receiveDone.get()){
            try {
                synchronized (this) {
                    wait();
                }
            }
            catch (InterruptedException e) {
                System.out.println("Interrupt exception during wait; continuing");
            }
        }

        Message received = null;
        try {

            received = targetReceived.get();
        }
        catch (NoSuchElementException e) {
            Assertions.fail();
        }
        Assertions.assertNotNull(received);
        Assertions.assertEquals(msg.toString(), received.toString()); // Compare just the content, not the peer member
        Assertions.assertTrue(target.stop());
        Assertions.assertTrue(server.stop());
    }

    @Test
    void testSendMultipleQueuedMessages() {
        int port = 8000;
        PeerConfiguration targetConfiguration = new PeerConfiguration(1001, "localhost", port, false);
        PeerConfiguration selfConfiguration = new PeerConfiguration(1002, "localhost", port + 1, false);
        Message[] messages = {
                new HaveMessage(1, targetConfiguration),
                new RequestMessage(2, targetConfiguration),
                new InterestedMessage(targetConfiguration)
        };

        BlockingQueue<Message> targetReceivedQueue = new LinkedBlockingQueue<>();
        final ServerTests instance = this;

        Server target = new Server(targetConfiguration, selfConfiguration, true, (Message m) -> {
            System.out.printf("Expected message info: %s%n", m.info());
            targetReceivedQueue.add(m);

        });

        Server server = new Server(selfConfiguration /* dummy */, targetConfiguration, false, (Message m) -> {});
        AtomicReference<Boolean> transmissionDone = new AtomicReference<>(false);

        Thread t = new Thread(() -> {
            Assertions.assertTrue(server.start());
            try {
                for (Message message : messages) {
                    Assertions.assertTrue(server.sendMessage(message));
                }
                transmissionDone.set(true);
                synchronized (instance) {
                    instance.notify();
                }
            }
            catch (Exception e) {
                Assertions.fail(e.toString());
            }
        });
        t.start();

        Assertions.assertTrue(target.start());

        // Wait till the message is received to check equality
        while (!transmissionDone.get()){
            try {
                synchronized (this) {
                    wait();
                }
            }
            catch (InterruptedException e) {
                System.out.println("Interrupt exception during wait; continuing");
            }
        }

        Message received = null;
        try {
            int i = 0;
            while (!targetReceivedQueue.isEmpty()) {
                received = targetReceivedQueue.remove();
                Assertions.assertEquals(messages[i].toString(), received.toString()); // Compare just the content, not the peer member
                i++;
            }
        }
        catch (NoSuchElementException e) {
            Assertions.fail();
        }
        Assertions.assertTrue(target.stop());
        Assertions.assertTrue(server.stop());
    }

    @Test
    void testSendMultipleQueuedMessagesConcurrentHandling() {
        int port = 8000;
        PeerConfiguration targetConfiguration = new PeerConfiguration(1001, "localhost", port, false);
        PeerConfiguration selfConfiguration = new PeerConfiguration(1002, "localhost", port + 1, false);
        Message[] messages = {
                new HaveMessage(1, targetConfiguration),
                new RequestMessage(2, targetConfiguration),
                new InterestedMessage(targetConfiguration)
        };

        BlockingQueue<Message> targetReceivedQueue = new LinkedBlockingQueue<>();
        final ServerTests instance = this;

        Server target = new Server(targetConfiguration, selfConfiguration, true, (Message m) -> {
            targetReceivedQueue.add(m);
            synchronized (instance) {
                instance.notify();
            }
        });

        Server server = new Server(selfConfiguration /* dummy */, targetConfiguration, false, (Message m) -> {});

        Thread t = new Thread(() -> {
            Assertions.assertTrue(server.start());
            try {
                for (Message message : messages) {
                    Assertions.assertTrue(server.sendMessage(message));
                }
            }
            catch (Exception e) {
                Assertions.fail(e.toString());
            }
        });
        t.start();

        Assertions.assertTrue(target.start());

        // Wait till the message is received to check equality
        int messagesHandled = 0;
        while (messagesHandled < messages.length){
            try {
                Message received = targetReceivedQueue.take();
                received.setPeer(targetConfiguration); // Change the peer for equality check
                System.out.printf("Message received: %s\n", received.info());
                System.out.printf("Message expected: %s\n", messages[messagesHandled].info());
                Assertions.assertEquals(messages[messagesHandled], received);
            }
            catch (InterruptedException e) {
                System.out.println("Interrupt exception during wait; continuing");
            }
            catch (NoSuchElementException e) {
                Assertions.fail();
            }

            messagesHandled++;
        }

        Assertions.assertTrue(target.stop());
        Assertions.assertTrue(server.stop());
    }

    @Test
    void testServerStartFailure() {
        int port = 8000;
        PeerConfiguration self = new PeerConfiguration(1001, "localhost", port, false);

        Server server = new Server(self, PEER1, false, (Message m) -> {});

        Assertions.assertFalse(server.start());
    }
}
