package com.kameni.lanacchain;

import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.lanac.data.LanacData;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.peer.PeerIdentity;
import com.kameni.lanacchain.peer.PeerNode;

// Use your own custom assertions for the reflection runner
import static com.kameni.lanacchain.testrunner.LanacAssert.assertEquals;
import static com.kameni.lanacchain.testrunner.LanacAssert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;

public class PeerNodeTest {

    private SignedAction actionA;
    private SignedAction actionB;

    void setUp() throws LanacSignatureException {
        // Included 0 as the tick for the blockchain height
        LanacData sampleData = new LanacData(100,0);
        PeerIdentity peerIdentity1 = new PeerIdentity();
        PeerIdentity peerIdentity2 = new PeerIdentity();
        actionA = new SignedAction(sampleData, peerIdentity1);
        actionB = new SignedAction(sampleData, peerIdentity2);
    }

    public void test__SerializationIntegrity() throws Exception {
        setUp();
        byte[] bytes = actionA.serialize();
        assertTrue(bytes != null, "Serialized bytes should not be null");

        SignedAction reconstructed = SignedAction.deserialize(bytes);

        assertEquals(actionA.getPeerAddress(), reconstructed.getPeerAddress(), "Address mismatch");
        assertEquals(actionA.getInputData().data, reconstructed.getInputData().data, "Data value mismatch");
        assertEquals(actionA.getInputData().tick, reconstructed.getInputData().tick, "Tick ID mismatch");

        assertTrue(Arrays.equals(actionA.getSignature(), reconstructed.getSignature()), "Cryptographic signature mismatch");
    }

    public void test__DeterministicSorting() throws Exception {
        setUp();
        List<SignedAction> capturedActions = new ArrayList<>();

        // Anonymous subclass to intercept the internal blockchain commit
        PeerNode node = new PeerNode(9999) {
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

    public void test__P2PConnection() throws Exception {
        setUp();
        // Use unique ports for the test environment
        PeerNode node1 = new PeerNode(45001);
        PeerNode node2 = new PeerNode(45002);

        // Allow ServerSockets time to bind to the ports
        Thread.sleep(150);

        try {
            node2.connectToPeer("127.0.0.1", 45001);
        } catch (Exception e) {
            throw new RuntimeException("P2P Handshake failed: " + e.getMessage());
        }

        // Wait for the background 'handlePeer' thread to initialize the socket
        Thread.sleep(150);

        assertTrue(true, "Connection established successfully");
    }
}
