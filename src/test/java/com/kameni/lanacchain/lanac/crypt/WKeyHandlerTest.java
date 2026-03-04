package com.kameni.lanacchain.lanac.crypt;


import com.kameni.lanacchain.exceptions.LanacKeyConversionException;
import com.kameni.lanacchain.peer.PeerIdentity;
import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.annotations.TestClass;

import java.security.PublicKey;

import static com.kameni.lanacchain.testrunner.LanacAssert.assertThrows;
import static com.kameni.lanacchain.testrunner.LanacAssert.assertTrue;

@TestClass
public class WKeyHandlerTest {

    @Test
    public void test__toPublicKey() throws LanacKeyConversionException {
        PeerIdentity peerIdentity = new PeerIdentity();
        PublicKey key = peerIdentity.getPublicKey();
        String key_string = WKeyHandler.toString(key);
        PublicKey reconverted_key = WKeyHandler.toPublicKey(key_string);

        IO.println(key.toString());
        IO.println(reconverted_key.toString());


        assertTrue(key.equals(reconverted_key), "Keys arent Matching");
    }

    @Test
    public void test__toPublicKeyError() {
        // string which cant be converted to Public Key
        String key_string = "rara u nana";
        assertThrows(LanacKeyConversionException.class, () -> WKeyHandler.toPublicKey(key_string), "Throws LanacKeyConversionException");
    }

}
