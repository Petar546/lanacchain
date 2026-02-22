package com.kameni.lanacchain;

import com.kameni.lanacchain.exceptions.KeyPairGenerationException;

import java.io.StringWriter;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Wallet {

    public PrivateKey privateKey;
    public PublicKey publicKey;
    private String data;

    public Wallet(String data){
        try {

            KeyPair keyPair = createKeyPairs();
            extractKeysFromKeyPair(keyPair);
        }catch (KeyPairGenerationException e){
            IO.println(e.getMessage());
        }

    }

    private KeyPair createKeyPairs() throws KeyPairGenerationException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            throw new KeyPairGenerationException(e.getMessage(), e.getCause());
        }
    }

    private void extractKeysFromKeyPair(KeyPair pair) {
        this.privateKey = pair.getPrivate(); // usually X.509 format
        this.publicKey = pair.getPublic(); // usually PKCS#8 format
    }
}
