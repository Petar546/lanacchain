package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.exceptions.LanacDeserializationException;
import com.kameni.lanacchain.exceptions.LanacPeerConnectionException;
import com.kameni.lanacchain.lanac.Lanac;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.peer.listeners.PeerNodeCommitListener;
import com.kameni.lanacchain.peer.listeners.PeerNodeConnectionListener;
import com.kameni.lanacchain.peer.listeners.PeerNodeListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerNode {
    public final List<Socket> peerConnections = Collections.synchronizedList(new ArrayList<>());
    protected PeerNodeListenerManager peerNodeListenerManager;
    private int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    private final Map<Long, List<SignedAction>> tickBuffer = new ConcurrentHashMap<>();
    private long currentProcessingTick = 0;
    private final int initPort;

    private PeerNode(int port) {
        this.initPort = port;
        this.peerNodeListenerManager = new PeerNodeListenerManager();
    }

    private void ensureRunning() {
        if (!running) {
            throw new IllegalStateException("PeerNode is not running. Call start() first.");
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            synchronized (peerConnections) {
                for (Socket s : peerConnections) {
                    if (!s.isClosed()) s.close();
                }
                peerConnections.clear();
            }
        } catch (IOException e) {
            System.err.println("Error during stop: " + e.getMessage());
        }
    }

    // ACTING AS SERVER
    private void listenForPeers(int listenPort) throws LanacPeerConnectionException {
        try {
            this.serverSocket = new ServerSocket(listenPort);
            this.port = serverSocket.getLocalPort();

            peerNodeListenerManager.notifyAllConnectionListeners(PeerNodeConnectionListener::onPortChosen, this.port);

            while (running) {
                Socket socket = serverSocket.accept();
                peerConnections.add(socket);
                peerNodeListenerManager.notifyAllConnectionListeners(PeerNodeConnectionListener::onPeerJoined, socket);
                new Thread(() -> handlePeer(socket)).start();
            }
        } catch (IOException e) {
            if (running) throw new LanacPeerConnectionException(e);
        }
    }

    // ACTING AS CLIENT
    public void connectToPeer(String ip, int peerPort) throws LanacPeerConnectionException {
        ensureRunning();
        try {
            Socket socket = new Socket(ip, peerPort);
            peerConnections.add(socket);
            peerNodeListenerManager.notifyAllConnectionListeners(PeerNodeConnectionListener::onConnectedToPeer, socket);
            new Thread(() -> handlePeer(socket)).start();
        } catch (IOException e) {
            throw new LanacPeerConnectionException(e);
        }
    }

    // Handle incoming data
    private void handlePeer(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            while (running && !socket.isClosed()) {
                int length = in.readInt();
                byte[] inputData = new byte[length];
                in.readFully(inputData);

                SignedAction action = SignedAction.deserialize(inputData);

                if (verifyIncomingAction(action)) {
                    addToPendingBuffer(action);
                }
            }
        } catch (IOException e) {
            if (running) {
                peerConnections.remove(socket);
                peerNodeListenerManager.notifyAllConnectionListeners(PeerNodeConnectionListener::onPeerDisconnected, socket);

            }
        } catch (LanacDeserializationException e) {
            // remove peer if disconnects
            System.err.println("Error during deserialization of data for Action");
            throw new RuntimeException("Error during deserialization of data for Action", e);
        }
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

    private void addToPendingBuffer(SignedAction action) {
        ensureRunning();
        long tick = action.getInputData().tick();

        tickBuffer.computeIfAbsent(tick, k -> Collections.synchronizedList(new ArrayList<>()))
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
            if (peerNodeListenerManager.hasCommitListeners()) {
                peerNodeListenerManager.notifyAllCommitListeners(PeerNodeCommitListener::onTryProcessTick, actionsThisTick);

            } else {
                throw new RuntimeException("Cant commit To Local Chain because no listener is present");
            }

            tickBuffer.remove(tick);
            currentProcessingTick++;
            tryProcessTick(currentProcessingTick);
        }
    }


    public void broadcastAction(SignedAction action) {
        ensureRunning();
        byte[] serializedAction = action.serialize();
        synchronized (peerConnections) {
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

    public void addListener(PeerNodeListener listener) {
        peerNodeListenerManager.addListener(listener);
    }

    public void removeListener(PeerNodeListener listener) {
        peerNodeListenerManager.removeListener(listener);
    }

    public static class Builder {
        private int port = 0;
        private final PeerNodeCommitListener mandatoryCommitListener;
        private final List<PeerNodeConnectionListener> connectionListeners = new ArrayList<>();

        // Private constructor to force use of the static 'with' method
        private Builder(PeerNodeCommitListener commitListener) {
            this.mandatoryCommitListener = commitListener;
        }

        /**
         * Entry point: The compiler forces you to provide a CommitListener here.
         */
        public static Builder withCommitListener(PeerNodeCommitListener listener) {
            if (listener == null) throw new IllegalArgumentException("CommitListener is required");
            return new Builder(listener);
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder addConnectionListener(PeerNodeConnectionListener listener) {
            if (listener != null) this.connectionListeners.add(listener);
            return this;
        }

        public PeerNode buildAndStart() {
            PeerNode node = new PeerNode(this.port);
            node.addListener(this.mandatoryCommitListener);

            for (PeerNodeConnectionListener cl : connectionListeners) {
                node.addListener(cl);
            }

            node.running = true;
            new Thread(() -> {
                try {
                    node.listenForPeers(node.initPort);
                } catch (LanacPeerConnectionException e) {
                    System.err.println("PeerNode Startup Error: " + e.getMessage());
                }
            }).start();

            return node;
        }
    }

}
