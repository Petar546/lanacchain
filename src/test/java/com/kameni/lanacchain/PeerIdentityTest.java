package com.kameni.lanacchain;

import com.kameni.lanacchain.lanac.Lanac;
import com.kameni.lanacchain.lanac.data.LanacData;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.peer.PeerIdentity;
import static com.kameni.lanacchain.testrunner.LanacAssert.assertTrue;

public class PeerIdentityTest {
    public void test__peerIdentityTest() throws Exception{
        PeerIdentity peer1 = new PeerIdentity();
        IO.println("Peer Address: " + peer1.getPeerAddress());

        // create data
        LanacData action1 = new LanacData(1, System.currentTimeMillis());

        // sign action
        SignedAction signedAction = new SignedAction(action1, peer1);

        // verify
        boolean isAuthentic = Lanac.verifyAction(signedAction);
        IO.println("Verified: " + isAuthentic);

        assertTrue(isAuthentic, "action is not authentic");

    }

}
