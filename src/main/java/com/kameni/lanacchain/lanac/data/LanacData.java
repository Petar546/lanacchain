package com.kameni.lanacchain.lanac.data;

import java.nio.ByteBuffer;

public record LanacData(int data, long tick, long otuNumber) {

    public static LanacData fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        int data = buffer.getInt();
        long timestamp = buffer.getLong();
        long otuNumber = buffer.getLong();

        return new LanacData(data, timestamp, otuNumber);
    }

    // converts object to byte array for signing
    public byte[] toBytes() {
        return ByteBuffer.allocate(20) // int 4 + long 8 + long 8
                .putInt(data)
                .putLong(tick)
                .putLong(otuNumber)
                .array();
    }
}
