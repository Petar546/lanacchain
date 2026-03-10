package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.lanac.Lanac;
import com.kameni.lanacchain.lanac.data.LanacData;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.annotations.TestClass;

import java.util.List;

import static com.kameni.lanacchain.testrunner.LanacAssert.assertTrue;

@TestClass
public class PeerTest {

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
    public void test__DeterministicSorting() throws Exception {
        setUp();
        Peer peer = new Peer();

        // Add actions to verify the sorting logic inside PeerNode
        List<SignedAction> unsorted = List.of(actionA, actionB);
        List<SignedAction> sorted = peer.sortActions(unsorted);

        String first = sorted.get(0).getPeerAddress();
        String second = sorted.get(1).getPeerAddress();

        // Lexicographical check: ensures all peers process in alphabetical order
        assertTrue(first.compareTo(second) <= 0, "Blockchain determinism failed: Peer addresses are not sorted");
    }

    @Test
    public void test__commitToLocalChain(){
        Peer peer = new Peer();

        // Add actions to verify the sorting logic inside PeerNode
        List<SignedAction> unsorted = List.of(actionA, actionB);
        List<SignedAction> sorted = peer.sortActions(unsorted);

        peer.commitToLocalChain(unsorted);
        Lanac lanac = peer.getLanac();

        for (int i = 0; i < lanac.getBlockchainSize(); i++) {
            String blockDisplay = String.format("Block[%d] Hash: %s | Prev: %s",
                    i,
                    lanac.getBlockAtIndex(i).getHash(),
                    lanac.getBlockAtIndex(i).getPreviousHash()
            );

            IO.println(blockDisplay);
        }
        assertTrue(lanac.isChainValid(), "Chain isnt valid");
        assertTrue(lanac.getBlockchainSize() == 3, "Blockchain size doesnt match");



    }


}
