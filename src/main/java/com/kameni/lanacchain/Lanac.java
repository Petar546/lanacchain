package com.kameni.lanacchain;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

public class Lanac {

    public ArrayList<Block> blockchain = new ArrayList<>();

    public Lanac() {
        blockchain.add(new Block("Genesis", "0"));
    }

    public void addBlock(LanacData data, byte[] signature, String sender) {
        String prevHash = blockchain.get(blockchain.size() - 1).hash;
        // We pass the data and signature into the block
        blockchain.add(new Block(data, prevHash));
    }

    public boolean isChainValid() {
        for (int i = 1; i < blockchain.size(); i++) {
            Block current = blockchain.get(i);
            Block previous = blockchain.get(i - 1);

            // 1. Check if hashes match
            if (!current.hash.equals(current.calculateHash())) return false;
            if (!current.previousHash.equals(previous.hash)) return false;

            // 2. Cryptographic Verification
            try {
                if (!verifyAction(current.data, current.signature, current.senderAddress)) {
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
