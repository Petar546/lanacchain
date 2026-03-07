package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.exceptions.LanacDeserializationException;
import com.kameni.lanacchain.exceptions.LanacPeerConnectionException;
import com.kameni.lanacchain.lanac.Lanac;
import com.kameni.lanacchain.lanac.data.SignedAction;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerNode {

    public List<Socket> peerConnections = new ArrayList<>();
    private PeerConnectionListener listener;
    private int port;

    // Hash map of Signed actions happening and their in the buffer
    private final Map<Long, List<SignedAction>> tickBuffer = new ConcurrentHashMap<>();
    private long currentProcessingTick = 0;

    public PeerNode(){
        int autoAllocatePort = 0;
        initPeerNode(autoAllocatePort, null);
    }

    public PeerNode(PeerConnectionListener listener){
        int autoAllocatePort = 0;
        initPeerNode(autoAllocatePort, listener);
    }
    public PeerNode(int port, PeerConnectionListener listener) {
        initPeerNode(port, listener);
    }

    private void initPeerNode(int port, PeerConnectionListener listener){
        this.port = port;
        this.listener = listener;
        new Thread(() -> {
            try {
                listenForPeers();
            } catch (LanacPeerConnectionException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public Optional<PeerConnectionListener> getListener() {
        return Optional.ofNullable(listener);
    }

    public int getPort() {
        return port;
    }

    public boolean verifyIncomingAction(SignedAction action) {
        try {
            return Lanac.verifyAction(action);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Interface for the Blockchain
     */
    public void commitToLocalChain(List<SignedAction> verifiedActions) {
        // 1. Sort by address (for network-wide consensus)
        // 2. Then sort by nonce (to process one player's moves in order)
        List<SignedAction> sortedActions = verifiedActions.stream()
                .sorted(Comparator.comparing(SignedAction::getPeerAddress)
                        .thenComparingLong((signedAction) -> {
                            return signedAction.getInputData().otuNumber();
                        } ))
                .toList();

        System.out.println(sortedActions);
        // Proceed with hashing and appending to the local blockchain
    }

    // ACTING AS SERVER
    private void listenForPeers() throws LanacPeerConnectionException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            //set port to the chosen port(not 0)
            port = serverSocket.getLocalPort();
            getListener().ifPresent(l -> l.onPortChosen(port));

            while (true) {
                Socket socket = serverSocket.accept();
                peerConnections.add(socket);
                getListener().ifPresent(l -> l.onPeerJoined(socket));
                new Thread(() -> handlePeer(socket)).start();
            }
        } catch (IOException e) {
            throw new LanacPeerConnectionException(e);
        }
    }

    // ACTING AS CLIENT
    public void connectToPeer(String ip, int peerPort) throws LanacPeerConnectionException {
        try {
            Socket socket = new Socket(ip, peerPort);
            peerConnections.add(socket);
            getListener().ifPresent(l -> l.onConnectedToPeer(socket));
            new Thread(() -> handlePeer(socket)).start();
        } catch (IOException e) {
            throw new LanacPeerConnectionException(e);
        }
    }

    // Handle incoming data
    private void handlePeer(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            while (!socket.isClosed()) {
                int length = in.readInt();
                byte[] inputData = new byte[length];
                in.readFully(inputData);

                SignedAction action = SignedAction.deserialize(inputData);

                if (verifyIncomingAction(action)) {
                    addToPendingBuffer(action);
                }
            }
        } catch (IOException e) {
            // remove peer if disconnects
            peerConnections.remove(socket);
            getListener().ifPresent(l -> l.onPeerDisconnected(socket));
            System.err.println("Peer disconnected. Remaining: " + peerConnections.size());
        } catch (LanacDeserializationException e) {
            // remove peer if disconnects
            System.err.println("Error during deserialization of data for Action");
            throw new RuntimeException("Error during deserialization of data for Action" , e);
        }
    }



    private void addToPendingBuffer(SignedAction action) {
        long tick = action.getInputData().tick();

        //add to buffer
        tickBuffer.computeIfAbsent(tick, k -> java.util.Collections.synchronizedList(new ArrayList<>()))
                .add(action);

        tryProcessTick(currentProcessingTick);
    }



    private boolean isTickComplete(long tick) {
        List<SignedAction> actionsThisTick = tickBuffer.get(tick);

        return actionsThisTick != null && actionsThisTick.size() >= (peerConnections.size() + 1);
    }


    private void tryProcessTick(long tick) {
        List<SignedAction> actionsThisTick = tickBuffer.get(tick);

        if (isTickComplete(tick)) {
            commitToLocalChain(actionsThisTick);

            tickBuffer.remove(tick);
            currentProcessingTick++;

            tryProcessTick(currentProcessingTick);
        }
    }


    public void broadcastAction(SignedAction action) {
        byte[] serializedAction = action.serialize();
        for (Socket socket : peerConnections) {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeInt(serializedAction.length);
                out.write(serializedAction);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException("Disconnect", e);
            }
        }
    }
}