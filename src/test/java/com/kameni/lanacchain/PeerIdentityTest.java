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
        boolean isAuthentic = verifyAction(action1, signature, peer1.getPeerAddress());
        IO.println("Verified: " + isAuthentic);

        assert isAuthentic;

    }

    /**
     * simulate peer verifying another peers LanacData
     */
    public static boolean verifyAction(LanacData data, byte[] signature, String address) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] publicBytes = Base64.getDecoder().decode(address);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(pubKey);
        sig.update(data.toBytes());
        return sig.verify(signature);
    }
}
