package com.kameni.lanacchain.peer.node;

import com.kameni.lanacchain.exceptions.LanacPeerConnectionException;
import com.kameni.lanacchain.lanac.Lanac;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.managers.PeerNodeListenerManager;
import com.kameni.lanacchain.peer.NodeInputHandler;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeBroadcastListener;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeCommitListener;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeConnectionListener;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientNode implements NodeInputHandler {
    public final List<Socket> peerConnections = Collections.synchronizedList(new ArrayList<>());
    private PeerNodeListenerManager listenerManager;
    private volatile boolean running = false;

    private final Map<Long, List<SignedAction>> tickBuffer = new ConcurrentHashMap<>();
    private long currentProcessingTick = 0;

    private ClientNode() {
        this.listenerManager = new PeerNodeListenerManager();
    }

    private void ensureRunning() {
        if (!running) {
            throw new IllegalStateException("PeerNode is not running. Call start() first.");
        }
    }

    public void stop() {
        running = false;
        try {
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

    // ACTING AS CLIENT
    public void connectToPeer(String ip, int peerPort) throws LanacPeerConnectionException {
        ensureRunning();
        try {
            Socket socket = new Socket(ip, peerPort);
            peerConnections.add(socket);
            listenerManager.notifyAll(PeerNodeConnectionListener.class, PeerNodeConnectionListener::onConnectedToPeer, socket);

            Runnable onException = () -> {
                peerConnections.remove(socket);
                listenerManager.notifyAll(PeerNodeConnectionListener.class, PeerNodeConnectionListener::onPeerDisconnected, socket);

            };
            new Thread(() -> handlePeer(socket, this::addActionToPendingBuffer, onException)).start();
        } catch (IOException e) {
            throw new LanacPeerConnectionException(e);
        }
    }

    private void addActionToPendingBuffer(SignedAction action){
        if (verifyIncomingAction(action)) {
            addToPendingBuffer(action);
        }
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

    // TODO: this Client Server Node stuff doesnt work, combine them properly
    // the client sets the broadcast but im not sure where it ends up, at the serverNode? what does it do with it? why are there two broadcast and tryProcessTick
    private void tryProcessTick(long tick) {
        List<SignedAction> actionsThisTick = tickBuffer.get(tick);

        if (isTickComplete(tick)) {
            if (listenerManager.hasListeners(PeerNodeCommitListener.class)) {
                listenerManager.notifyAll(PeerNodeCommitListener.class, PeerNodeCommitListener::onTryProcessTick, actionsThisTick);

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
                    listenerManager.notifyAll(PeerNodeBroadcastListener.class, PeerNodeBroadcastListener::onBroadcast, socket);
                } catch (IOException e) {
                    throw new RuntimeException("Disconnect", e);
                }
            }
        }
    }

    public PeerNodeListenerManager getListenerManager() {
        return listenerManager;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public static class Builder {
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
            return this;
        }

        public Builder addConnectionListener(PeerNodeConnectionListener listener) {
            if (listener != null) this.connectionListeners.add(listener);
            return this;
        }

        public ClientNode buildAndStart() {
            ClientNode node = new ClientNode();
            node.listenerManager.addListener(this.mandatoryCommitListener);

            for (PeerNodeConnectionListener cl : connectionListeners) {
                node.listenerManager.addListener(cl);
            }

            node.running = true;

            return node;
        }
    }

}
