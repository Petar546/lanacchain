package com.kameni.lanacchain.peer;

import com.kameni.lanacchain.exceptions.LanacDeserializationException;
import com.kameni.lanacchain.lanac.data.SignedAction;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

public interface NodeInputHandler {
    public boolean running = false;
    //TODO: handle peer in a node interface as deafault
    //TODO: ServerNode should process the data, create a block and append, Client node should just
    // Handle incoming data as client
    default void handlePeer(Socket socket, Consumer<SignedAction> verifyAndAddToBuffer, Runnable onException) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            while (running && !socket.isClosed()) {
                int length = in.readInt();
                byte[] inputData = new byte[length];
                in.readFully(inputData);

                SignedAction action = SignedAction.deserialize(inputData);
                verifyAndAddToBuffer.accept(action);

            }
        } catch (IOException e) {
            if (running) {

            }
        } catch (LanacDeserializationException e) {
            // remove peer if disconnects
            System.err.println("Error during deserialization of data for Action");
            throw new RuntimeException("Error during deserialization of data for Action", e);
        }
    }

}
