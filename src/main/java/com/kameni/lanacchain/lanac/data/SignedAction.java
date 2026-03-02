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

        int addressLength = buffer.getInt();
        if (addressLength <= 0 || addressLength > 392) { // Sanity check for a String address
            throw new LanacDeserializationException("Invalid address length: " + addressLength);
        }
        byte[] addressBytes = new byte[addressLength];
        buffer.get(addressBytes);
        String address = new String(addressBytes, StandardCharsets.UTF_8);

        int dataLen = buffer.getInt();
        if (dataLen != 12) {
            throw new LanacDeserializationException("Invalid LanacData length. Expected 20, got: " + dataLen);
        }
        byte[] dataBytes = new byte[dataLen];
        buffer.get(dataBytes);
        LanacData lanacData = LanacData.fromBytes(dataBytes);

        if (!buffer.hasRemaining()) throw new LanacDeserializationException("Buffer underflow: Missing signature length");
        int sigLen = buffer.getInt();
        if (sigLen <= 0 || sigLen > 1024) { // Signatures are usually 64-512 bytes
            throw new LanacDeserializationException("Invalid signature length: " + sigLen);
        }

        if (buffer.remaining() < sigLen) {
            throw new LanacDeserializationException("Malformed packet: Signature truncated");
        }

        byte[] signature = new byte[sigLen];
        buffer.get(signature);

        return new SignedAction(lanacData, address, signature);
    }

}
