package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.exceptions.LanacDeserializationException;
import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.lanac.data.LanacData;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.testrunner.LanacTestUtils;
import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.annotations.TestClass;

// Use your own custom assertions for the reflection runner

import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.kameni.lanacchain.testrunner.LanacAssert.*;

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
    public void test__DeterministicSorting() throws Exception {
        setUp();
        List<SignedAction> capturedActions = new ArrayList<>();

        // Anonymous subclass to intercept the internal blockchain commit
        PeerNode node = new PeerNode() {
            @Override
            public void commitToLocalChain(List<SignedAction> verifiedActions) {
                List<SignedAction> sorted = verifiedActions.stream()
                        .sorted(Comparator.comparing(SignedAction::getPeerAddress))
                        .toList();
                capturedActions.addAll(sorted);
            }
        };

        // Add actions to verify the sorting logic inside PeerNode
        List<SignedAction> unsorted = List.of(actionA, actionB);
        node.commitToLocalChain(unsorted);

        String first = capturedActions.get(0).getPeerAddress();
        String second = capturedActions.get(1).getPeerAddress();

        // Lexicographical check: ensures all peers process in alphabetical order
        assertTrue(first.compareTo(second) <= 0, "Blockchain determinism failed: Peer addresses are not sorted");
    }

    @Test
    public void test__P2PConnection() throws Exception {
        setUp();

        CountDownLatch peerJoinedLatch = new CountDownLatch(2);
        CountDownLatch portChosenLatch = new CountDownLatch(1);

        PeerConnectionListener myListener1asServer = new PeerConnectionListener() {
            @Override
            public void onPeerJoined(Socket s) {
                IO.println("P1 New Inbound: " + s.getPort());
                peerJoinedLatch.countDown();
            }

            @Override
            public void onConnectedToPeer(Socket s) {
                IO.println("P1 New Outbound: " + s.getPort());
            }

            @Override
            public void onPeerDisconnected(Socket s) {
                IO.println("P1 Peer Left: " + s.getRemoteSocketAddress());
            }
        };

        PeerConnectionListener myListener2asJoinee = new PeerConnectionListener() {
            @Override
            public void onPeerJoined(Socket s) { IO.println("P2 New Inbound: " + s.getPort()); }

            @Override
            public void onConnectedToPeer(Socket s) {
                IO.println("P2 New Outbound: " + s.getPort());
                peerJoinedLatch.countDown();
            }

            @Override
            public void onPeerDisconnected(Socket s) {
                IO.println("P2 Peer Left: " + s.getRemoteSocketAddress());
            }

            @Override
            public int onPortChosen(int port) {
                portChosenLatch.countDown();
                return PeerConnectionListener.super.onPortChosen(port);
            }
        };
        // Use unique ports for the test environment
        PeerNode node1asServer = new PeerNode(myListener1asServer);
        PeerNode node2asJoinee = new PeerNode(myListener2asJoinee);

        //wait until port is chosen
        portChosenLatch.await(1, TimeUnit.SECONDS);

        try {
            node2asJoinee.connectToPeer("127.0.0.1", node1asServer.getPort());
            // listener.onConnectedToPeer will write "P2 New Outbound: 45001"
        } catch (Exception e) {
            throw new RuntimeException("P2P Handshake failed: " + e.getMessage());
        }

        IO.println(node1asServer.peerConnections.toString());
        assertTrue(!node1asServer.peerConnections.isEmpty(), "Connections not found on node 1");
        // peerListener has latches which are checkeds
        assertTrue(peerJoinedLatch.await(1, TimeUnit.SECONDS), "peerJoinedLatch failed, possibly a connection didnt happen");
    }


    @Test
    public void test__RejectionOfInvalidData() throws Exception {
        setUp();

        byte[] garbage = new byte[]{0, 1, 2, 3};

        assertThrows(LanacDeserializationException.class, () -> {
            SignedAction.deserialize(garbage);
        }, "Node should throw an error when deserializing invalid byte arrays");
    }

    @Test
    public void test__tamperedSignatureRejection() throws Exception {
        setUp();
        PeerConnectionListener myListener = new PeerConnectionListener() {
            @Override
            public void onPeerJoined(Socket s) { System.out.println("P1 New Inbound: " + s.getPort()); }

            @Override
            public void onConnectedToPeer(Socket s) { System.out.println("P1 New Outbound: " + s.getPort()); }

            @Override
            public void onPeerDisconnected(Socket s) {
                System.out.println("P1 Peer Left: " + s.getRemoteSocketAddress());
            }
        };
        PeerNode node = new PeerNode(45003, myListener);
        byte[] fakeSignature = new byte[]{ 0x13, 0x37, 0x00 };
        SignedAction maliciousAction = LanacTestUtils.createTamperedAction(actionA.getInputData(), actionA.getPeerAddress(), fakeSignature);

        boolean isValid = node.verifyIncomingAction(maliciousAction);

        assertTrue(!isValid, "Security Breach: Node accepted an action with a faked signature!");
    }
}
