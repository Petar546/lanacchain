package com.kameni.lanacchain;


public class Main {
    static void main() {
        IO.println(String.format("LanacChain!"));

        Lanac.createBlocks();

        //invalid Block addition
        Lanac.blockchain.add(new Block("Invalid test", "rara"));

        if (Lanac.isChainValid()){

            //display blocks
            StringBuilder blockDisplay;
            for (int i = 0; i < Lanac.blockchain.size(); i++) {
                blockDisplay = new StringBuilder();
                blockDisplay.append("Hash -> ");
                blockDisplay.append(Lanac.blockchain.get(i).hash);
                blockDisplay.append(" previous hash [");
                blockDisplay.append(Lanac.blockchain.get(i).previousHash);
                blockDisplay.append("]");

                IO.println(blockDisplay.toString());
            }
        }else{
            IO.println("Chain aint valid");
        }

    }
}
