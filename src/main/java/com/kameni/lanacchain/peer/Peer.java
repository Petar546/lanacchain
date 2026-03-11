package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.exceptions.LanacPeerConnectionException;
import com.kameni.lanacchain.lanac.Lanac;
import com.kameni.lanacchain.lanac.data.SignedAction;

import java.net.Socket;
import java.util.Comparator;
import java.util.List;

public class Peer {
    private PeerIdentity peerIdentity;
    private PeerNode peerNode;
    private Lanac lanac = new Lanac();
    private boolean isStarted = false;

    private PeerConnectionListener peerConnectionListener = new PeerConnectionListener() {
        @Override
        public void onPeerJoined(Socket socket) {
            IO.println("Peer joined on Socket " + socket);
            PeerConnectionListener.super.onPeerJoined(socket);
        }

        @Override
        public void onConnectedToPeer(Socket socket) {
            IO.println("Connected to peer on Socket " + socket);
        }

        @Override
        public void onPeerDisconnected(Socket socket) {
            IO.println("Peer disconeccted Socket " + socket);
        }

        @Override
        public int onPortChosen(int port) {
            IO.println("Port " + port + " has been chosen by Peer");
            return port;
        }

        @Override
        public void onCommitToLocalChain(List<SignedAction> actionsToCommitToLocalChain) {
            commitToLocalChain(actionsToCommitToLocalChain);
        }
    };

    public Peer() {
        peerIdentity = new PeerIdentity();
        peerNode = PeerNode.createAndStart();
    }

    public void start() {
        if (isStarted) return;
        peerNode.createAndStart();
        peerNode.addListener(peerConnectionListener);
        isStarted = true;
    }

    /**
     * Safety check helper
     */
    private void ensureStarted() {
        if (!isStarted || peerNode == null) {
            throw new IllegalStateException("Peer must be started via start() before performing operations.");
        }
    }

    protected List<SignedAction> sortActions(List<SignedAction> actionsToSort) {
        ensureStarted();
        // 1. address 2. nonce
        List<SignedAction> sortedActions = actionsToSort.stream()
                .sorted(Comparator.comparing(SignedAction::getPeerAddress)
                        .thenComparingLong((signedAction) -> {
                            return signedAction.getInputData().otuNumber();
                        }))
                .toList();

        System.out.println(sortedActions);
        return sortedActions;
    }

    /**
     * Interface for the Blockchain
     */
    public void commitToLocalChain(List<SignedAction> verifiedActions) {
        ensureStarted();
        List<SignedAction> sortedActions = sortActions(verifiedActions);
        // Proceed with hashing and appending to the local blockchain
        sortedActions.forEach((a) -> {
            lanac.addBlock(a);
        });
    }

    protected Lanac getLanac(){
        return lanac;
    }

    protected PeerNode getPeerNode() {
        return peerNode;
    }

    protected void connectToPeer(String ip, int port)  {
        ensureStarted();
        try {
            peerNode.connectToPeer(ip, port);

        } catch (LanacPeerConnectionException e) {
            IO.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
