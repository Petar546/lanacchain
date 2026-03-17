package com.kameni.lanacchain.peer.node;

import com.kameni.lanacchain.exceptions.LanacDeserializationException;
import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.lanac.data.LanacData;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.peer.PeerIdentity;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeCommitListener;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeConnectionListener;
import com.kameni.lanacchain.LanacTestUtils;
import com.kameni.lanactest.annotations.Test;
import com.kameni.lanactest.annotations.TestClass;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.kameni.lanactest.LanacAssert.*;

@TestClass
public class PeerNodeTest {

    private SignedAction actionA;
    private SignedAction actionB;
    private PeerIdentity peerIdentity1;
    private PeerIdentity peerIdentity2;

    void setUp() throws LanacSignatureException {
        // Included 0 as the tick for the blockchain height
        LanacData sampleData = new LanacData(100,0, 5);

        peerIdentity1 = new PeerIdentity();
        peerIdentity2 = new PeerIdentity();

        actionA = new SignedAction(sampleData, peerIdentity1);
        actionB = new SignedAction(sampleData, peerIdentity2);
    }

    @Test
    public void test__SerializationIntegrity() throws Exception {
        setUp();
        byte[] bytes = actionA.serialize();
        assertTrue(bytes != null, "Serialized bytes should not be null");

        SignedAction reconstructed = SignedAction.deserialize(bytes);

        assertEquals(actionA.getPeerAddress(), reconstructed.getPeerAddress(), "Address mismatch");
        assertEquals(actionA.getInputData().data(), reconstructed.getInputData().data(), "Data value mismatch");
        assertEquals(actionA.getInputData().tick(), reconstructed.getInputData().tick(), "Tick ID mismatch");

        assertTrue(Arrays.equals(actionA.getSignature(), reconstructed.getSignature()), "Cryptographic signature mismatch");
    }


    @Test
    public void test__P2PConnection() throws Exception {
        setUp();

        CountDownLatch peerJoinedLatch = new CountDownLatch(2);
        CountDownLatch node1Ready = new CountDownLatch(1);

        PeerNodeConnectionListener myListener1asServer = new PeerNodeConnectionListener() {
            @Override
            public void onPeerJoined(Socket s) {
                peerJoinedLatch.countDown();
            }
            @Override
            public int onPortChosen(int port) {
                node1Ready.countDown();
                return port;
            }
        };

        PeerNodeConnectionListener myListener2asJoinee = new PeerNodeConnectionListener() {
            @Override
            public void onConnectedToPeer(Socket s) {
                peerJoinedLatch.countDown();
            }
        };

        PeerNodeCommitListener ignoredCommitListener = actionsToCommitToLocalChain -> {};
        ServerNode node1asServer = ServerNode.Builder
                .withCommitListener(ignoredCommitListener)
                .addConnectionListener(myListener1asServer)
                .buildAndStart();

        ClientNode node2asJoinee = ClientNode.Builder
                .withCommitListener(ignoredCommitListener)
                .addConnectionListener(myListener2asJoinee)
                .buildAndStart();

        try {
            // Wait for server to actually bind to a port
            assertTrue(node1Ready.await(2, TimeUnit.SECONDS), "node1asServer failed to bind port");

            // Act
            node2asJoinee.connectToPeer("127.0.0.1", node1asServer.getPort());

            // Assert
            assertTrue(peerJoinedLatch.await(3, TimeUnit.SECONDS),
                    "Handshake timed out. Latch count: " + peerJoinedLatch.getCount());

            assertFalse(node1asServer.peerConnections.isEmpty(), "node1asServer has no connections");

        } finally {
            node1asServer.stop();
            node2asJoinee.stop();
        }
    }


    @Test
    public void test__RejectionOfInvalidData() throws Exception {
        setUp();

        byte[] garbage = new byte[]{0, 1, 2, 3};

        assertThrows(LanacDeserializationException.class, () -> SignedAction.deserialize(garbage), "Node should throw an error when deserializing invalid byte arrays");
    }

    @Test
    public void test__tamperedSignatureRejection() throws Exception {
        setUp();
        PeerNodeConnectionListener myListener = new PeerNodeConnectionListener() {
            @Override
            public void onPeerJoined(Socket s) { System.out.println("P1 New Inbound: " + s.getPort()); }

            @Override
            public void onConnectedToPeer(Socket s) { System.out.println("P1 New Outbound: " + s.getPort()); }

            @Override
            public void onPeerDisconnected(Socket s) {
                System.out.println("P1 Peer Left: " + s.getRemoteSocketAddress());
            }
        };
        PeerNodeCommitListener ignoredCommitListener = actionsToCommitToLocalChain -> {};

        ClientNode node = ClientNode.Builder
                .withCommitListener(ignoredCommitListener)
                .addConnectionListener(myListener)
                .setPort(45003)
                .buildAndStart();
        node.getListenerManager().addListener(myListener);

        byte[] fakeSignature = new byte[]{ 0x13, 0x37, 0x00 };
        SignedAction maliciousAction = LanacTestUtils.createTamperedAction(actionA.getInputData(), actionA.getPeerAddress(), fakeSignature);

        boolean isValid = node.verifyIncomingAction(maliciousAction);

        assertTrue(!isValid, "Security Breach: Node accepted an action with a faked signature!");
    }
}
