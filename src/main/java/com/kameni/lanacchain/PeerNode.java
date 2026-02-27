package com.kameni.lanacchain;

import java.security.PublicKey;
import java.security.Signature;
import java.util.List;

public class PeerNode {
    // Verifies that Peer B's move is authentic
    public boolean verifyIncomingAction(SignedAction action, PublicKey peerPublicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(peerPublicKey);
            sig.update(action.inputData.toBytes());
            return sig.verify(action.signature);
        } catch (Exception e) {
            return false;
        }
    }

    // Interface for the Blockchain: Packages verified actions into a Block
    public void commitToLocalChain(List<SignedAction> verifiedActions) {
        // 1. Sort actions by playerAddress to ensure determinism
        // 2. Create a hash of all actions
        // 3. Append to your local blockchain copy
    }
}