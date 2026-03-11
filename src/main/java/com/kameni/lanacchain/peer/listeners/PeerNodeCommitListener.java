package com.kameni.lanacchain.peer.listeners;

import com.kameni.lanacchain.lanac.data.SignedAction;

import java.util.List;

public interface PeerNodeCommitListener extends PeerNodeListener {

    /**
     *  Listener call for commitToLocalChain for Peer
     * @param actionsToCommitToLocalChain
     */
    void onTryProcessTick(List<SignedAction> actionsToCommitToLocalChain);
}
