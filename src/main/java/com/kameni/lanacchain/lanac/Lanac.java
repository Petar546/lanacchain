package com.kameni.lanacchain.lanac;

import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.peer.PeerIdentity;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

public class Lanac {

    public ArrayList<Block> blockchain = new ArrayList<>();

    public Lanac() {
        LanacData data = new LanacData(0, new Date().getTime());
        PeerIdentity genesisPeer = new PeerIdentity();
        String previoushash = "0";

        SignedAction genesisAction;
        try {
            genesisAction = new SignedAction(data, genesisPeer);

        }catch (LanacSignatureException e){
            throw new RuntimeException(e);
        }
        blockchain.add(new Block(genesisAction, previoushash));
    }

    public void addBlock(SignedAction signedAction) {
        String prevHash = blockchain.getLast().getHash();
        // We pass the data and signature into the block
        blockchain.add(new Block(signedAction, prevHash));
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
                if (!verifyAction(current.getSignedAction())) {
                    IO.println("Block " + i + " has a fraudulent signature!");
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static boolean verifyAction(SignedAction signedAction) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] publicBytes = Base64.getDecoder().decode(signedAction.getPeerAddress());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(pubKey);
        sig.update(signedAction.getInputData().toBytes());
        return sig.verify(signedAction.getSignature());
    }
}
