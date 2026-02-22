package com.kameni;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyConverter {

    public static String toString(PublicKey publicKey) {

        byte[] publicKeyByte = publicKey.getEncoded();

        return  Base64.getEncoder().encodeToString(publicKeyByte);
    }

    public static String toString(PrivateKey privateKey) {

        System.out.println("PUBLIC KEY::" + privateKey);

        //converting public key to byte
        byte[] privateKeyByte = privateKey.getEncoded();
        System.out.println("\nBYTE KEY::: " + privateKeyByte);

        //converting byte to String
        String privateKeyString = Base64.getEncoder().encodeToString(privateKeyByte);
        // String privateKeyString = new String(privateKeyByte,Charset.);
        System.out.println("\nSTRING KEY::" + privateKeyString);

        return privateKeyString;
    }

    public static PublicKey toPublicKey(String publicKeyString) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {

        //converting string to Bytes
        byte[] publicKeyByte  = Base64.getDecoder().decode(publicKeyString);
        System.out.println("BYTE KEY::" + publicKeyByte);


        //converting it back to public key
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(publicKeyByte));
        System.out.println("FINAL OUTPUT" + publicKey);
        return publicKey;
    }

    public static PrivateKey toPrivateKey(String privateKeyString) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {

        //converting string to Bytes
        byte[] privateKeyByte  = Base64.getDecoder().decode(privateKeyString);
        System.out.println("BYTE KEY::" + privateKeyByte);


        //converting it back to public key
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = factory.generatePrivate(new X509EncodedKeySpec(privateKeyByte));
        System.out.println("FINAL OUTPUT" + privateKey);
        return privateKey;
    }
}
