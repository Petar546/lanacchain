package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.exceptions.LanacDeserializationException;
import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.lanac.data.LanacData;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.testrunner.LanacTestUtils;
import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.annotations.TestClass;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.kameni.lanacchain.testrunner.LanacAssert.*;

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


}
