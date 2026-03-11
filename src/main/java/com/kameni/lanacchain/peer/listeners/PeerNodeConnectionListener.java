package com.kameni.lanacchain.peer.listeners;

import java.net.Socket;

public interface PeerNodeConnectionListener extends PeerNodeListener{

    default void onPeerJoined(Socket socket) {}

    default void onConnectedToPeer(Socket socket) {}

    default void onPeerDisconnected(Socket socket) {}

    default int onPortChosen(int port){
        return port;
    }

}
