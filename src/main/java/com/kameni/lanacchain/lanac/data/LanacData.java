package com.kameni.lanacchain.lanac.data;

import java.nio.ByteBuffer;

public class LanacData {
    public int data;
    public long tick;

    public LanacData(int data, long tick){
        this.data = data;
        this.tick = tick;
    }

    public static LanacData fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        int data = buffer.getInt();
        long timestamp = buffer.getLong();

        return new LanacData(data, timestamp);
    }

    // converts object to byte array for signing
    public byte[] toBytes() {
        return ByteBuffer.allocate(12) // int 4 + long 8 bytes
            .putInt(data)
            .putLong(tick)
            .array();
    }
}
