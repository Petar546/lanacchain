package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.lanac.Lanac;
import com.kameni.lanacchain.lanac.data.LanacData;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeBroadcastListener;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeConnectionListener;
import com.kameni.lanactest.annotations.Test;
import com.kameni.lanactest.annotations.TestClass;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.kameni.lanactest.LanacAssert.assertTrue;

@TestClass
public class PeerTest {

    private SignedAction actionA;
    private SignedAction actionB;
    private PeerIdentity peerIdentity1;
    private PeerIdentity peerIdentity2;

    void setUp() throws LanacSignatureException {
        // Included 0 as the tick for the blockchain height
        LanacData sampleData = new LanacData(100, 0, 5);

        peerIdentity1 = new PeerIdentity();
        peerIdentity2 = new PeerIdentity();

        actionA = new SignedAction(sampleData, peerIdentity1);
        actionB = new SignedAction(sampleData, peerIdentity2);
    }

    @Test(name = "DeterministicSorting")
    public void test__DeterministicSorting() throws Exception {
        setUp();
        Peer peer = new Peer.Builder().buildAndStart();
        // Add actions to verify the sorting logic inside PeerNode
        List<SignedAction> unsorted = List.of(actionA, actionB);
        List<SignedAction> sorted = peer.sortActions(unsorted);

        String first = sorted.get(0).getPeerAddress();
        String second = sorted.get(1).getPeerAddress();

        // Lexicographical check: ensures all peers process in alphabetical order
        assertTrue(first.compareTo(second) <= 0, "Blockchain determinism failed: Peer addresses are not sorted");
    }

    @Test
    public void test__commitToLocalChain() throws Exception{
        setUp();
        Peer peer = new Peer.Builder().buildAndStart();
        // Add actions to verify the sorting logic inside PeerNode
        List<SignedAction> unsorted = List.of(actionA, actionB);

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

    @Test
    public void test__peerConnectAndShareData() throws Exception{
        setUp();

        CountDownLatch portsFoundLatch = new CountDownLatch(2);
        CountDownLatch broadcastLatch = new CountDownLatch(2);
        PeerNodeConnectionListener p1Listener = new PeerNodeConnectionListener() {
            @Override
            public int onPortChosen(int port) {
                portsFoundLatch.countDown();
                IO.println("p1Listener port found");
                return PeerNodeConnectionListener.super.onPortChosen(port);
            }
        };
        PeerNodeConnectionListener p2Listener = new PeerNodeConnectionListener() {

            @Override
            public int onPortChosen(int port) {
                portsFoundLatch.countDown();
                IO.println("p2Listener port found");
                return PeerNodeConnectionListener.super.onPortChosen(port);
            }
        };

        PeerNodeBroadcastListener broadcastListener1 = new PeerNodeBroadcastListener() {
            @Override
            public Socket onBroadcast(Socket socket){
                IO.println("broadcast1 happened");
                broadcastLatch.countDown();
                return socket;
            };
        };
        PeerNodeBroadcastListener broadcastListener2 = new PeerNodeBroadcastListener() {
            @Override
            public Socket onBroadcast(Socket socket){
                IO.println("broadcast2 happened");
                broadcastLatch.countDown();
                return socket;
            };
        };

        Peer peer1 = new Peer.Builder()
                .customConnectionListener(p1Listener)
                .buildAndStart();


        Peer peer2 = new Peer.Builder()
                .customConnectionListener(p2Listener)
                .buildAndStart();

        assertTrue(portsFoundLatch.await(4, TimeUnit.SECONDS), "Ports havent been assigned");

        int peerNode1port = peer1.getServerNode().getPort();
        int peerNode2port = peer2.getServerNode().getPort();


        peer1.getClientNode().getListenerManager().addListener(broadcastListener1);
        peer2.getClientNode().getListenerManager().addListener(broadcastListener2);

        // wait for the port to be assigned
        peer1.connectToPeer("127.0.0.1", peerNode2port);
        peer2.connectToPeer("127.0.0.1", peerNode1port);
        peer1.getClientNode().broadcastAction(actionA);
        peer2.getClientNode().broadcastAction(actionB);
        assertTrue(broadcastLatch.await(4, TimeUnit.SECONDS), "Broadcast hasnt happened");

        Lanac lanac1 = peer1.getLanac();
        Lanac lanac2 = peer2.getLanac();


        for (int i = 0; i < lanac1.getBlockchainSize(); i++) {
            String blockDisplay = String.format("Block[%d] Hash: %s | Prev: %s",
                    i,
                    lanac1.getBlockAtIndex(i).getHash(),
                    lanac1.getBlockAtIndex(i).getPreviousHash()
            );

            IO.println(blockDisplay);
        }


        assertTrue(lanac1.isChainValid(), "Chain1 isnt valid");
        assertTrue(lanac2.isChainValid(), "Chain2 isnt valid");

        //size 2 cause first is origin block
        assertTrue(lanac1.getBlockchainSize() == 2, "Blockchain1 size doesnt match");
        assertTrue(lanac2.getBlockchainSize() == 2, "Blockchain2 size doesnt match");


    }
}
