package com.kameni.lanacchain;


import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class LanacChain {
    static void main() throws Exception {
        IO.println("LanacChain!");
        Lanac lanac = new Lanac();
        //invalid Block addition
//        lanac.blockchain.add(new Block("Invalid test", "rara"));

        if (lanac.isChainValid()) {

            //display blocks
            for (int i = 0; i < lanac.blockchain.size(); i++) {
                String blockDisplay = String.format("Block[%d] Hash: %s | Prev: %s",
                        i,
                        lanac.blockchain.get(i).hash,
                        lanac.blockchain.get(i).previousHash);

                IO.println(blockDisplay);
            }
        }else{
            IO.println("Chain aint valid");
        }
    }


}
