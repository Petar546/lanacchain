package com.kameni.lanacchain;


import com.kameni.lanacchain.lanac.Lanac;
import com.kameni.lanacchain.lanac.LanacData;
import com.kameni.lanacchain.exceptions.LanacSignatureException;
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
        LanacData action1 = new LanacData(1, System.currentTimeMillis());

        byte[] signature1;
        try {
            // sign action
            signature1 = peer1.signData(action1);
            IO.println("Action signed. Signature length: " + signature1.length);

        }catch (LanacSignatureException e){
            throw new RuntimeException(e);
        }

        lanac.addBlock(action1, signature1, peer1.getPeerAddress());

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
