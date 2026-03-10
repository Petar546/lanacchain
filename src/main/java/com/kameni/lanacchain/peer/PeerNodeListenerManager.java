package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.lanac.data.SignedAction;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PeerNodeListenerManager {
    private final List<PeerConnectionListener> listeners = new ArrayList<>();

    public void addListener(PeerConnectionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(PeerConnectionListener listener) {
        listeners.remove(listener);
    }

    public boolean hasListeners(){
        return !listeners.isEmpty();
    }

    protected <T> void notifyAll(BiConsumer<PeerConnectionListener, T> method, T arguments) {
        for (PeerConnectionListener listener : listeners) {
            method.accept(listener, arguments);
        }
    }
}
