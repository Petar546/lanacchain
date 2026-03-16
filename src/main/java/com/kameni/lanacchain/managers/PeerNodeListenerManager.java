package com.kameni.lanacchain.managers;

import com.kameni.lanacchain.peer.node.listeners.Listener;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeCommitListener;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeConnectionListener;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeListener; // Assuming this is your base
import java.util.List;

public class PeerNodeListenerManager extends ListenerManager<PeerNodeListener> {

    @Override
    protected List<Class<? extends PeerNodeListener>> getAllowedListeners() {
        return List.of(
            PeerNodeConnectionListener.class,
            PeerNodeCommitListener.class
        );
    }
}
