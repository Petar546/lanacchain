package com.kameni.lanacchain;


import com.kameni.lanacchain.exceptions.LanacKeyConversionException;
import com.kameni.lanacchain.lanac.crypt.WKeyHandler;
import com.kameni.lanacchain.peer.PeerIdentity;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class KeyConverterTest {

    public void test__toPublicKey() throws LanacKeyConversionException {
        PeerIdentity peerIdentity = new PeerIdentity();
        PublicKey key = peerIdentity.getPublicKey();
        String key_string = WKeyHandler.toString(key);
        PublicKey reconverted_key = WKeyHandler.toPublicKey(key_string);

        IO.println(key.toString());
        IO.println(reconverted_key.toString());

        assert key.equals(reconverted_key);
    }
}
