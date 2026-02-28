package com.kameni.lanacchain;

import javax.crypto.NullCipher;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

public class Lanac {

    public ArrayList<Block> blockchain = new ArrayList<>();

    public Lanac() {
        LanacData genesis = new LanacData(0, new Date().getTime());
        byte[] signature = new byte[0];
        String senderAdress = "0";
        String previoushash = "0";
        blockchain.add(new Block(genesis, signature, senderAdress, previoushash));
    }

    public void addBlock(LanacData data, byte[] signature, String senderAddress) {
        String prevHash = blockchain.getLast().getHash();
        // We pass the data and signature into the block
        blockchain.add(new Block(data, signature, senderAddress, prevHash));
    }

    public boolean isChainValid() {
        for (int i = 1; i < blockchain.size(); i++) {
            Block current = blockchain.get(i);
            Block previous = blockchain.get(i - 1);

            // 1. Check if hashes match
            if (!current.getHash().equals(current.calculateHash())) return false;
            if (!current.getPreviousHash().equals(previous.getHash())) return false;

            // 2. Cryptographic Verification
            try {
                if (!verifyAction(current.getData(), current.getSignature(), current.getSenderAddress())) {
                    IO.println("Block " + i + " has a fraudulent signature!");
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static boolean verifyAction(LanacData data, byte[] signature, String address) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] publicBytes = Base64.getDecoder().decode(address);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(pubKey);
        sig.update(data.toBytes());
        return sig.verify(signature);
    }
}
