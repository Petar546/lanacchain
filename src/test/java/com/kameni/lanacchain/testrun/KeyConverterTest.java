package com.kameni.lanacchain.testrun;


import com.kameni.lanacchain.exceptions.LanacKeyConversionException;
import com.kameni.lanacchain.lanac.crypt.WKeyHandler;
import com.kameni.lanacchain.peer.PeerIdentity;
import java.security.PublicKey;

import static com.kameni.lanacchain.testrunner.LanacAssert.assertThrows;
import static com.kameni.lanacchain.testrunner.LanacAssert.assertTrue;

public class KeyConverterTest {

    public void test__toPublicKey() throws LanacKeyConversionException {
        PeerIdentity peerIdentity = new PeerIdentity();
        PublicKey key = peerIdentity.getPublicKey();
        String key_string = WKeyHandler.toString(key);
        PublicKey reconverted_key = WKeyHandler.toPublicKey(key_string);

        IO.println(key.toString());
        IO.println(reconverted_key.toString());


        assertTrue(key.equals(reconverted_key), "Keys arent Matching");
    }


    public void test__toPublicKeyError() {
        // string which cant be converted to Public Key
        String key_string = "rara u nana";
        assertThrows(LanacKeyConversionException.class, () -> WKeyHandler.toPublicKey(key_string), "Throws LanacKeyConversionException");
    }

}
