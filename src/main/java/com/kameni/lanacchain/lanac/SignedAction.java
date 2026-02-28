package com.kameni.lanacchain.lanac;

import com.kameni.lanacchain.peer.PeerIdentity;

public class SignedAction {
    public String peerAddress;
    public LanacData inputData;
    public byte[] signature;

    public SignedAction(LanacData inputData, PeerIdentity id) throws Exception {
        this.peerAddress = id.getPeerAddress();
        this.inputData = inputData;

        //signing
        this.signature = id.signData(inputData);
    }
}
