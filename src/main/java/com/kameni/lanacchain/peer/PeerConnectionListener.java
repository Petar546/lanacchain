package com.kameni.lanacchain.peer;

import java.net.Socket;

public interface PeerConnectionListener {

    default void onPeerJoined(Socket socket) {}

    default void onConnectedToPeer(Socket socket) {}

    default void onPeerDisconnected(Socket socket) {}

    default int onPortChosen(int port){
        return port;
    }
}
