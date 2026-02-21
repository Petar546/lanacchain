package com.kameni.lanacchain;

import java.util.Date;

public class Block {
    public String hash;
    public String previousHash;
    private String data;
    private long timeStamp;

    public Block(String data, String previousHash){
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash(){
        //calculating hash using previous hash, timestamp and data
        String hash = Crypt.sha256( previousHash + Long.toString(timeStamp) + data);
        return hash;
    }

}
