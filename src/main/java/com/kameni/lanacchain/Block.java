package com.kameni.lanacchain;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

public class Block {
    private final String hash;
    private final String previousHash;
    private final LanacData data;
    private final byte[] signature;
    private final String senderAddress;
    private final long timeStamp;

    // regular block constructor
    public Block(LanacData data, byte[] signature, String senderAddress, String previousHash){
        this.data = data;
        this.signature = signature;
        this.senderAddress = senderAddress;
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
        this.hash = calculateHash();
    }

    public String calculateHash(){
        // combine
        String input = previousHash +
            Long.toString(timeStamp) +
            (data != null ? Base64.getEncoder().encodeToString(data.toBytes()) : "") +
            (signature != null ? Base64.getEncoder().encodeToString(signature) : "") +
            (senderAddress != null ? senderAddress : "");

        //calculating hash using previous hash, timestamp, data, signature, and sender adress
        return Crypt.sha256( input);
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public LanacData getData() {
        return data;
    }

    public byte[] getSignature() {
        return signature;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
