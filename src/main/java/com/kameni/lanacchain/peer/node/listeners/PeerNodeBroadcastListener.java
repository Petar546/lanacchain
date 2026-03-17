package com.kameni.lanacchain.peer.node.listeners;

import java.net.Socket;

public interface PeerNodeBroadcastListener extends PeerNodeListener {

    /**
     *  Listener call for when broadcast happens and on which socket
     * @param socket
     */
    default Socket onBroadcast(Socket socket){
        return socket;
    };
}
