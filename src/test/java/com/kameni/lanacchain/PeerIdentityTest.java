package com.kameni.lanacchain;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PeerIdentityTest {
    public void test__peerIdentityTest() throws Exception{
        PeerIdentity peer1 = new PeerIdentity();
        IO.println("Peer Address: " + peer1.getPeerAddress());

        // create data
        LanacData action1 = new LanacData(1, System.currentTimeMillis());

        // sign action
        byte[] signature = peer1.signData(action1);
        IO.println("Action signed. Signature length: " + signature.length);

        // verify
        boolean isAuthentic = Lanac.verifyAction(action1, signature, peer1.getPeerAddress());
        IO.println("Verified: " + isAuthentic);

        assert isAuthentic;

    }

}
