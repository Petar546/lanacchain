package com.kameni.lanacchain.peer.listeners;

import com.kameni.lanacchain.lanac.data.SignedAction;

import java.util.List;

public interface PeerNodeCommitListener extends PeerNodeListener {

    void onCommitToLocalChain(List<SignedAction> actionsToCommitToLocalChain);
}
