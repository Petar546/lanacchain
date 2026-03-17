package com.kameni.lanacchain.managers;

import com.kameni.lanacchain.peer.node.listeners.*;

import java.util.List;

public class PeerNodeListenerManager extends ListenerManager<PeerNodeListener> {

    @Override
    protected List<Class<? extends PeerNodeListener>> getAllowedListeners() {
        return List.of(
            PeerNodeConnectionListener.class,
            PeerNodeCommitListener.class,
            PeerNodeBroadcastListener.class
        );
    }
}
