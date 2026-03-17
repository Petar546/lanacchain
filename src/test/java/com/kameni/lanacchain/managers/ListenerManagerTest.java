package com.kameni.lanacchain.managers;

import com.kameni.lanacchain.peer.node.listeners.Listener;
import com.kameni.lanactest.annotations.Test;
import com.kameni.lanactest.annotations.TestClass;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.kameni.lanactest.LanacAssert.assertTrue;
import static com.kameni.lanactest.LanacAssert.assertFalse;

@TestClass
public class ListenerManagerTest {

    // Mock interfaces for testing
    interface TestBaseListener extends Listener {}
    interface SpecificListener extends TestBaseListener {
        void onEvent(String data);
    }
    interface OtherListener extends TestBaseListener {}

    // impl for testing
    class TestManager extends ListenerManager<TestBaseListener> {
        @Override
        protected List<Class<? extends TestBaseListener>> getAllowedListeners() {
            return List.of(SpecificListener.class);
        }
    }

    @Test
    public void test__addListenerAndNotify() throws Exception {
        TestManager manager = new TestManager();
        AtomicBoolean wasCalled = new AtomicBoolean(false);

        // Create a listener implementation
        SpecificListener listener = (data) -> {
            if ("test-data".equals(data)) {
                wasCalled.set(true);
            }
        };

        // Add and check state
        manager.addListener(listener);
        assertTrue(manager.hasListeners(SpecificListener.class), "Manager should have SpecificListener");

        // Notify
        manager.notifyAll(SpecificListener.class, SpecificListener::onEvent, "test-data");
        assertTrue(wasCalled.get(), "Listener method was not called with correct data");
    }


    @Test
    public void test__addMultipleListenersAndNotify() throws Exception {
        TestManager manager = new TestManager();
        AtomicBoolean wasCalled1 = new AtomicBoolean(false);
        AtomicBoolean wasCalled2 = new AtomicBoolean(false);

        // Create a listener implementation
        SpecificListener listener1 = (data) -> {
            if ("test-data".equals(data)) {
                wasCalled1.set(true);
            }
        };
        SpecificListener listener2 = (data) -> {
            if ("test-data".equals(data)) {
                wasCalled2.set(true);
            }
        };

        // Add multiple and check state
        manager.addListener(listener1);
        manager.addListener(listener2);

        assertTrue(manager.hasListeners(SpecificListener.class), "Manager should have SpecificListener");

        // Notify
        manager.notifyAll(SpecificListener.class, SpecificListener::onEvent, "test-data");
        IO.println("wasCalled1:" + wasCalled1);
        IO.println("wasCalled2:" + wasCalled2);

        assertTrue(wasCalled1.get(), "Listener1 method was not called with correct data");
        assertTrue(wasCalled2.get(), "Listener2 method was not called with correct data");

    }

    @Test
    public void test__removeListener() throws Exception {
        TestManager manager = new TestManager();
        SpecificListener listener = (data) -> {};

        manager.addListener(listener);
        assertTrue(manager.hasListeners(SpecificListener.class), "Should have listener before removal");

        manager.removeListener(SpecificListener.class, listener);
        assertFalse(manager.hasListeners(SpecificListener.class), "Should not have listener after removal");
    }

    @Test
    public void test__unsupportedListenerThrowsException() throws Exception {
        TestManager manager = new TestManager();
        OtherListener unsupported = new OtherListener() {};

        boolean threw = false;
        try {
            manager.addListener(unsupported);
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        assertTrue(threw, "Adding an unsupported listener should throw IllegalArgumentException");
    }

    @Test
    public void test__notifyEmptyGroupDoesNotCrash() throws Exception {
        TestManager manager = new TestManager();

        // This should not throw NullPointerException
        manager.notifyAll(SpecificListener.class, SpecificListener::onEvent, "no-op");

        assertTrue(true, "Notification of empty group should be a no-op");
    }
}
