package com.kameni.lanacchain;


import com.kameni.KeyConverter;

public class LanacChain {
    static void main() {
        IO.println("LanacChain!");
        Wallet wallet = new Wallet("data");

        Lanac lanac = new Lanac();
        lanac.createBlocks();
        //invalid Block addition
//        lanac.blockchain.add(new Block("Invalid test", "rara"));

        if (lanac.isChainValid()){

            //display blocks
            StringBuilder blockDisplay;
            for (int i = 0; i < lanac.blockchain.size(); i++) {
                blockDisplay = new StringBuilder();
                blockDisplay.append("Hash -> ");
                blockDisplay.append(lanac.blockchain.get(i).hash);
                blockDisplay.append(" previous hash [");
                blockDisplay.append(lanac.blockchain.get(i).previousHash);
                blockDisplay.append("]");

                IO.println(blockDisplay.toString());
            }
        }else{
            IO.println("Chain aint valid");
        }

    }
}
