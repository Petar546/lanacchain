package com.kameni.lanacchain.managers;

import com.kameni.lanacchain.peer.node.listeners.PeerNodeConnectionListener;
import com.kameni.lanacchain.peer.node.listeners.PeerNodeListener;
import com.kameni.lanacchain.testrunner.annotations.Test;
import com.kameni.lanacchain.testrunner.annotations.TestClass;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

import static com.kameni.lanacchain.testrunner.LanacAssert.assertTrue;
import static com.kameni.lanacchain.testrunner.LanacAssert.assertFalse;

@TestClass
public class PeerNodeListenerManagerTest {

    @Test
    public void test__registrationAndNotification() throws Exception {
        PeerNodeListenerManager manager = new PeerNodeListenerManager();
        AtomicReference<String> capturedSocketData = new AtomicReference<>("");

        // 1. Create a concrete listener
        PeerNodeConnectionListener connectionListener = new PeerNodeConnectionListener() {
            @Override
            public void onConnectedToPeer(Socket socket) {
                capturedSocketData.set("Connected!");

            }
        };

        // 2. Add listener (This should trigger the hierarchy check)
        manager.addListener(connectionListener);

        assertTrue(manager.hasListeners(PeerNodeConnectionListener.class),
                "Manager should recognize the specific connection listener");

        // 3. Notify using the specific interface
        manager.notifyAll(
                PeerNodeConnectionListener.class,
                PeerNodeConnectionListener::onConnectedToPeer,
                null // Passing null as a mock socket
        );

        assertTrue("Connected!".equals(capturedSocketData.get()),
                "The listener method should have been executed via notifyAll");
    }

    @Test
    public void test__hierarchyDiscovery() throws Exception {
        PeerNodeListenerManager manager = new PeerNodeListenerManager();

        // PeerNodeConnectionListener extends PeerNodeListener
        PeerNodeConnectionListener connectionListener = new PeerNodeConnectionListener() {};

        manager.addListener(connectionListener);

        // Check if it was added to the PARENT bucket as well
        //TODO: if parent is allowed then it F*CKS me and doesnt work, FIX
        assertTrue(manager.hasListeners(PeerNodeListener.class),
                "Listener should be registered under its parent interface automatically");
    }

    @Test
    public void test__removalLogic() throws Exception {
        PeerNodeListenerManager manager = new PeerNodeListenerManager();
        PeerNodeConnectionListener listener = new PeerNodeConnectionListener() {};

        manager.addListener(listener);
        assertTrue(manager.hasListeners(PeerNodeConnectionListener.class), "Should exist");

        manager.removeListener(PeerNodeConnectionListener.class, listener);
        assertFalse(manager.hasListeners(PeerNodeConnectionListener.class), "Should be removed");
    }

    @Test
    public void test__unsupportedType() throws Exception {
        PeerNodeListenerManager manager = new PeerNodeListenerManager();

        // A listener that is NOT in the allowed list
        PeerNodeListener fakeListener = new PeerNodeListener() {};

        boolean errorThrown = false;
        try {
            manager.addListener(fakeListener);
        } catch (IllegalArgumentException e) {
            errorThrown = true;
        }

        assertTrue(errorThrown, "Should throw IllegalArgumentException for unsupported types");
    }
}
