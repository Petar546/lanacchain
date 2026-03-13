package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.exceptions.LanacPeerConnectionException;
import com.kameni.lanacchain.lanac.Lanac;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.peer.node.ServerNode;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeCommitListener;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeConnectionListener;
import com.kameni.lanacchain.peer.node.ClientNode;

import java.net.Socket;
import java.util.Comparator;
import java.util.List;

public class Peer {
    private PeerIdentity peerIdentity;
    private ClientNode clientNode;
    private ServerNode serverNode;
    private Lanac lanac;
    private boolean isStarted = false;

    private PeerNodeConnectionListener peerNodeConnectionListener = new PeerNodeConnectionListener() {
        @Override
        public void onPeerJoined(Socket socket) {
            IO.println("Peer joined on Socket " + socket);
            PeerNodeConnectionListener.super.onPeerJoined(socket);
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
    };

    private final PeerNodeCommitListener commitListener = new PeerNodeCommitListener() {
        @Override
        public void onTryProcessTick(List<SignedAction> actionsToCommitToLocalChain) {
            commitToLocalChain(actionsToCommitToLocalChain);
        }
    };

    private Peer() {
        this.peerIdentity = new PeerIdentity();
        this.lanac = new Lanac();
    }


    /**
     * Safety check helper
     */
    private void ensureStarted() {
        if (!isStarted || clientNode == null) {
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

    protected ClientNode getClientNode() {
        return clientNode;
    }

    protected ServerNode getServerNode() {
        return serverNode;
    }
    protected void connectToPeer(String ip, int port)  {
        ensureStarted();
        try {
            clientNode.connectToPeer(ip, port);

        } catch (LanacPeerConnectionException e) {
            IO.println(e.getMessage());
            e.printStackTrace();
        }
    }


    public static class Builder {
        private PeerNodeConnectionListener customConnectionListener;

        public Builder() {}

        public Builder customConnectionListener(PeerNodeConnectionListener listener) {
            this.customConnectionListener = listener;
            return this;
        }

        public Peer buildAndStart() {
            Peer peer = new Peer();
            if (this.customConnectionListener != null) {
                peer.peerNodeConnectionListener = this.customConnectionListener;
            }

            if (peer.isStarted){
                throw new IllegalStateException("Peer already started");
            };

            peer.clientNode = ClientNode.Builder
                    .withCommitListener(peer.commitListener)
                    .addConnectionListener(peer.peerNodeConnectionListener)
                    .buildAndStart();

            peer.serverNode = ServerNode.Builder
                    .withCommitListener(peer.commitListener)
                    .addConnectionListener(peer.peerNodeConnectionListener)
                    .buildAndStart();

            peer.isStarted = true;
            return peer;
        }
    }

}
