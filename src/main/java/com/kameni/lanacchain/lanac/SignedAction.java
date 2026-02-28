package com.kameni.lanacchain.lanac;

import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.peer.PeerIdentity;

public class SignedAction {
    private final String peerAddress;
    private final LanacData inputData;
    private final byte[] signature;

    public SignedAction(LanacData inputData, PeerIdentity id) throws LanacSignatureException {
        this.peerAddress = id.getPeerAddress();
        this.inputData = inputData;

        //signing
        this.signature = id.signData(inputData);
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
