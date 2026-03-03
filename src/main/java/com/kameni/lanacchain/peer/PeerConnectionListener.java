package com.kameni.lanacchain.peer;

import java.net.Socket;

public interface PeerConnectionListener {
    void onPeerJoined(Socket socket);
    void onConnectedToPeer(Socket socket);
    void onPeerDisconnected(Socket socket);
}
