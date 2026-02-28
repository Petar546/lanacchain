package com.kameni.lanacchain.lanac;

import com.kameni.lanacchain.lanac.crypt.Crypt;

import java.util.Base64;

public class Block {
    private final String hash;
    private final String previousHash;
    private final SignedAction signedAction;
    private final long timeStamp;

    // regular block constructor
    public Block(SignedAction signedAction, String previousHash){
        this.signedAction = signedAction;
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
        this.hash = calculateHash();
    }



    public String calculateHash(){
        // combine
        String input = previousHash +
            Long.toString(timeStamp) +
            (signedAction.getInputData() != null ? Base64.getEncoder().encodeToString(signedAction.getInputData().toBytes()) : "") +
            (signedAction.getSignature() != null ? Base64.getEncoder().encodeToString(signedAction.getSignature()) : "") +
            (signedAction.getPeerAddress() != null ? signedAction.getPeerAddress() : "");

        //calculating hash using previous hash, timestamp, data, signature, and sender adress
        return Crypt.sha256( input);
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public SignedAction getSignedAction() {
        return signedAction;
    }


    public long getTimeStamp() {
        return timeStamp;
    }
}
