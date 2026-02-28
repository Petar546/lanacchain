package com.kameni.lanacchain;


import com.kameni.lanacchain.lanac.Lanac;
import com.kameni.lanacchain.lanac.data.LanacData;
import com.kameni.lanacchain.exceptions.LanacSignatureException;
import com.kameni.lanacchain.lanac.data.SignedAction;
import com.kameni.lanacchain.peer.PeerIdentity;

public class LanacChain {
    static void main() {
        IO.println("LanacChain!");
        Lanac lanac = new Lanac();
        //invalid Block addition
//        lanac.blockchain.add(new Block("Invalid test", "rara"));

        PeerIdentity peer1 = new PeerIdentity();
        IO.println("Peer Address: " + peer1.getPeerAddress());

        // create data
        LanacData actionData1 = new LanacData(1, System.currentTimeMillis());

        SignedAction action1;
        try {
            // sign action
            action1 = new SignedAction(actionData1, peer1);
            IO.println("Action signed by peer: " + peer1.getPeerAddress());

        }catch (LanacSignatureException e){
            throw new RuntimeException(e);
        }

        lanac.addBlock(action1);

        //add bad block
        lanac.addBadBlock(action1);

        //add fraudulent block (doesnt work)
        PeerIdentity peer2 = new PeerIdentity();

        IO.println("Peer Address: " + peer2.getPeerAddress());

        // create data
        LanacData actionData2 = new LanacData(1, System.currentTimeMillis());

        SignedAction action2;
        try {
            // sign action
            action2 = new SignedAction(actionData2, peer2);
            IO.println("Action signed by peer: " + peer2.getPeerAddress());

        }catch (LanacSignatureException e){
            throw new RuntimeException(e);
        }

        //add fraudulent block
        lanac.addBlock(action2);

        if (lanac.isChainValid()) {

            //display blocks
            for (int i = 0; i < lanac.blockchain.size(); i++) {
                String blockDisplay = String.format("Block[%d] Hash: %s | Prev: %s",
                        i,
                        lanac.blockchain.get(i).getHash(),
                        lanac.blockchain.get(i).getPreviousHash());

                IO.println(blockDisplay);
            }
        }else{
            IO.println("Chain aint valid");
        }

    }


}
