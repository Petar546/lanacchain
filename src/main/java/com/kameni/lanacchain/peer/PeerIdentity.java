package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.lanac.crypt.WKeyHandler;
import com.kameni.lanacchain.lanac.LanacData;
import com.kameni.lanacchain.exceptions.LanacKeyPairGenerationException;
import com.kameni.lanacchain.exceptions.LanacSignatureException;

import java.security.*;
import java.util.Base64;


public class PeerIdentity {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public PeerIdentity() {
        try {

            KeyPair keyPair = WKeyHandler.createKeyPairs();
            extractKeysFromKeyPair(keyPair);
        } catch (LanacKeyPairGenerationException e) {
            IO.println(e.getMessage());
        }
    }

    // This is the "Address" other players use to identify you
    public String getPeerAddress() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public byte[] signData(LanacData data) throws LanacSignatureException {
        try {
            Signature dsa = Signature.getInstance("SHA256withRSA");
            dsa.initSign(privateKey);
            dsa.update(data.toBytes());
            return dsa.sign();

        }catch (InvalidKeyException | SignatureException| NoSuchAlgorithmException e) {
            throw new LanacSignatureException(e);
        }
    }

    private void extractKeysFromKeyPair(KeyPair pair) {
        this.privateKey = pair.getPrivate(); // usually X.509 format
        this.publicKey = pair.getPublic(); // usually PKCS#8 format
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
