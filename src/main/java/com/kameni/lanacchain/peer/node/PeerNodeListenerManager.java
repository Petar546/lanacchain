package com.kameni.lanacchain.peer.node;

import com.kameni.lanacchain.peer.node.listeners.PeerNodeCommitListener;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeConnectionListener;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PeerNodeListenerManager {
    private final List<PeerNodeConnectionListener> connectionListeners = new ArrayList<>();
    private final List<PeerNodeCommitListener> commitListeners = new ArrayList<>();

    public void addListener(PeerNodeListener listener) {
        if (listener instanceof PeerNodeConnectionListener){
            connectionListeners.add((PeerNodeConnectionListener) listener);

        } else if (listener instanceof PeerNodeCommitListener) {
            commitListeners.add((PeerNodeCommitListener) listener);
        }
    }

    public void removeListener(PeerNodeListener listener) {
        if (listener instanceof PeerNodeConnectionListener){
            connectionListeners.remove((PeerNodeConnectionListener) listener);

        } else if (listener instanceof PeerNodeCommitListener) {
            commitListeners.remove((PeerNodeCommitListener) listener);
        }
    }

    public boolean hasConnectionListeners(){
        return !connectionListeners.isEmpty();
    }
    public boolean hasCommitListeners(){
        return !commitListeners.isEmpty();
    }

    protected <T> void notifyAllConnectionListeners(BiConsumer<PeerNodeConnectionListener, T> method, T arguments) {
        for (PeerNodeConnectionListener listener : connectionListeners) {
            method.accept(listener, arguments);
        }
    }
    protected <T> void notifyAllCommitListeners(BiConsumer<PeerNodeCommitListener, T> method, T arguments) {
        for (PeerNodeCommitListener listener : commitListeners) {
            method.accept(listener, arguments);
        }
    }
}
