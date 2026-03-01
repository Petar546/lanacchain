package com.kameni.lanacchain.lanac.data;

import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.peer.PeerIdentity;

public class SignedAction {
    private final String peerAddress;
    private final LanacData inputData;
    private final byte[] signature;

    public SignedAction(LanacData inputData, PeerIdentity peer) throws LanacSignatureException {
        this.peerAddress = peer.getPeerAddress();
        this.inputData = inputData;

        //signing
        this.signature = peer.signData(inputData);
    }

    public String getPeerAddress() {
        return peerAddress;
    }

    public LanacData getInputData() {
        return inputData;
    }

    public byte[] getSignature() {
        return signature;
    }
}
