package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.lanac.Lanac;
import com.kameni.lanacchain.lanac.SignedAction;

import java.util.List;

public class PeerNode {
    // Verifies that Peers move is authentic
    public boolean verifyIncomingAction(SignedAction action) {
        try {
            return  Lanac.verifyAction(action);
        } catch (Exception e) {
            return false;
        }
    }

    // Interface for the Blockchain
    public void commitToLocalChain(List<SignedAction> verifiedActions) {
        // Sort actions by playerAddress to ensure determinism
        // Create a hash of all actions
        // Append to your local blockchain copy
    }
}