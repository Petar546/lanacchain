package com.kameni.lanacchain;


import com.kameni.KeyConverter;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class KeyConverterTest {

    public void test__toPublicKey() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        Wallet wallet = new Wallet("test");
        PublicKey key = wallet.publicKey;
        String key_string = KeyConverter.toString(key);
        PublicKey reconverted_key = KeyConverter.toPublicKey(key_string);

        IO.println(key.toString());
        IO.println(reconverted_key.toString());

        assert key.equals(reconverted_key);
    }
}
