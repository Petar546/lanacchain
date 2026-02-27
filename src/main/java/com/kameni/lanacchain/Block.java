package com.kameni.lanacchain;

import java.util.Date;

public class Block {
    public String hash;
    public String previousHash;
    private LanacData data;

    public Block(LanacData data, String previousHash){
        this.data = data;
        this.previousHash = previousHash;
        this.hash = calculateHash();
    }

    public String calculateHash(){
        //calculating hash using previous hash, timestamp and data
        return Crypt.sha256( previousHash + Long.toString(data.timestamp) + data);
    }

}
