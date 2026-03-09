package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.lanac.data.SignedAction;

import java.net.Socket;
import java.util.List;

public interface PeerConnectionListener {

    default void onPeerJoined(Socket socket) {}

    default void onConnectedToPeer(Socket socket) {}

    default void onPeerDisconnected(Socket socket) {}

    default int onPortChosen(int port){
        return port;
    }

    void onCommitToLocalChain(List<SignedAction> actionsToCommitToLocalChain);
}
