package com.kameni.lanacchain.lanac.crypt;

import com.kameni.lanacchain.exceptions.LanacKeyConversionException;
import com.kameni.lanacchain.exceptions.LanacKeyPairGenerationException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class WKeyHandler {


    public static KeyPair createKeyPairs() throws LanacKeyPairGenerationException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            throw new LanacKeyPairGenerationException(e.getMessage(), e.getCause());
        }
    }



    public static String toString(AsymmetricKey publicKey) {

        byte[] keyByte = publicKey.getEncoded();

        return  Base64.getEncoder().encodeToString(keyByte);
    }

    public static PublicKey toPublicKey(String keyString) throws LanacKeyConversionException {
        try {
            //converting string to Bytes
            byte[] keyByte  = Base64.getDecoder().decode(keyString);
            //converting it back to public key
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(keyByte));
            System.out.println("FINAL OUTPUT" + publicKey);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new LanacKeyConversionException(e);
        }
    }

    public static PrivateKey toPrivateKey(String keyString) throws LanacKeyConversionException {

        try {
            //converting string to Bytes
            byte[] keyByte  = Base64.getDecoder().decode(keyString);
            //converting back to private key
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = factory.generatePrivate(new X509EncodedKeySpec(keyByte));
            System.out.println("FINAL OUTPUT" + privateKey);
            return privateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new LanacKeyConversionException(e);
        }
    }
}
