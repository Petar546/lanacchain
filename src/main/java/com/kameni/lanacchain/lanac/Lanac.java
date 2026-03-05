package com.kameni.lanacchain.lanac;

import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.lanac.data.LanacData;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.peer.PeerIdentity;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;

public class Lanac {

    private final LinkedList<Block> blockchain = new LinkedList<>();

    public Lanac() {
        //creating genesisBlock for Chain
        LanacData data = new LanacData(0, new Date().getTime(), 2);
        PeerIdentity genesisPeer = new PeerIdentity();
        String previousHash = "0";

        SignedAction genesisAction;
        try {
            genesisAction = new SignedAction(data, genesisPeer);

        }catch (LanacSignatureException e){
            throw new RuntimeException(e);
        }
        blockchain.add(new Block(genesisAction, previousHash));
    }

    public void addBlock(SignedAction signedAction) {
        String prevHash = blockchain.getLast().getHash();
        blockchain.add(new Block(signedAction, prevHash));
    }

    public void addBadBlock(SignedAction signedAction) {
        blockchain.add(new Block(signedAction, "0"));
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

    public Block getBlockAtIndex(int index) {
        return blockchain.get(index);
    }

    public int getBlockchainSize() {
        return blockchain.size();
    }
}
