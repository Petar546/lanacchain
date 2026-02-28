package com.kameni.lanacchain.lanac.data;

import java.nio.ByteBuffer;

public class LanacData {
    public int data;
    public long timestamp;

    public LanacData(int data, long timestamp){
        this.data = data;
        this.timestamp = timestamp;
    }

    // converts object to byte array for signing
    public byte[] toBytes() {
        return ByteBuffer.allocate(12) // int 4 + long 8 bytes
            .putInt(data)
            .putLong(timestamp)
            .array();
    }
}
