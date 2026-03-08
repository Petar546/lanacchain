package com.kameni.lanacchain.lanac.data;

import com.kameni.lanacchain.exceptions.LanacDeserializationException;
import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.peer.PeerIdentity;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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

    /**
     * Only used for serialize and deserialize
     */
    private SignedAction(LanacData inputData, String peerAddress, byte[] signature) {
        this.peerAddress = peerAddress;
        this.inputData = inputData;
        this.signature = signature;
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

    /**
     * Converts this object into a byte[] to be sent over the network
     */
    public byte[] serialize() {
        byte[] addressBytes = peerAddress.getBytes(StandardCharsets.UTF_8);
        byte[] dataBytes = inputData.toBytes();

        int totalSize = 12 + addressBytes.length + dataBytes.length + signature.length;

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.putInt(addressBytes.length);
        buffer.put(addressBytes);

        buffer.putInt(dataBytes.length);
        buffer.put(dataBytes);

        buffer.putInt(signature.length);
        buffer.put(signature);

        return buffer.array();
    }

    /**
     * Reconstructs a SignedAction from a byte[] received by PeerNode
     */
    public static SignedAction deserialize(byte[] bytes) throws LanacDeserializationException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        String address = getAddressBytes(buffer);
        LanacData lanacData = getLanacDataBytes(buffer);
        byte[] signature = getSignatureBytes(buffer);

        return new SignedAction(lanacData, address, signature);
    }

    private static String getAddressBytes(ByteBuffer buffer) throws LanacDeserializationException {
        int addressLength = buffer.getInt();
        if (addressLength <= 0 || addressLength > 392) {
            throw new LanacDeserializationException("Invalid address length: " + addressLength);
        }
        byte[] addressBytes = new byte[addressLength];
        buffer.get(addressBytes);
        return new String(addressBytes, StandardCharsets.UTF_8);
    }

    private static LanacData getLanacDataBytes(ByteBuffer buffer) throws LanacDeserializationException {
        int dataLen = buffer.getInt();
        if (dataLen != 20) {
            throw new LanacDeserializationException("Invalid LanacData length. Expected 20, got: " + dataLen);
        }
        byte[] dataBytes = new byte[dataLen];
        buffer.get(dataBytes);
        return LanacData.fromBytes(dataBytes);
    }

    private static byte[] getSignatureBytes(ByteBuffer buffer) throws LanacDeserializationException {
        if (!buffer.hasRemaining()) {
            throw new LanacDeserializationException("Buffer underflow: Missing signature length");
        }
        int sigLen = buffer.getInt();
        if (sigLen <= 0 || sigLen > 1024) {
            throw new LanacDeserializationException("Invalid signature length: " + sigLen);
        }
        if (buffer.remaining() < sigLen) {
            throw new LanacDeserializationException("Malformed packet: Signature truncated");
        }
        byte[] signature = new byte[sigLen];
        buffer.get(signature);
        return signature;
    }
}
