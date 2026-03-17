package com.kameni.lanacchain.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A type-safe manager for handling groups of listeners.
 *
 * @param <B> The base listener interface (e.g., PeerNodeListener).
 */
public abstract class ListenerManager<B> {

    // Use List<?> to represent that the map holds lists of different specific types
    private final Map<Class<? extends B>, List<?>> listenerGroups = new HashMap<>();

    /**
     * Internal helper to add to the specific group
     */
    @SuppressWarnings("unchecked")
    private <L extends B> void addRawListener(Class<L> type, L listener) {
        List<L> group = (List<L>) listenerGroups.computeIfAbsent(type, _ -> new ArrayList<>());
        group.add(listener);
    }

    /**
     * checks if the listener matches any allowed sub-interfaces
     */
    @SuppressWarnings("unchecked")
    public void addListener(B listener) {
        for (Class<? extends B> allowedType : getAllowedListeners()) {
            if (allowedType.isInstance(listener)) {
                addRawListener((Class<B>) allowedType, listener);
                return;
            }
        }
        throw new IllegalArgumentException("Listener type not supported: " + listener.getClass());
    }

    public <L extends B> void removeListener(Class<L> type, L listener) {
        List<?> group = listenerGroups.get(type);
        if (group != null) {
            group.remove(listener);
        }
    }

    public boolean hasListeners(Class<? extends B> type) {
        List<?> listeners = listenerGroups.get(type);
        return listeners != null && !listeners.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public <L extends B, T> void notifyAll(Class<L> type, BiConsumer<L, T> method, T arguments) {
        List<L> listeners = (List<L>) listenerGroups.get(type);
        if (listeners != null) {
            for (L listener : listeners) {
                method.accept(listener, arguments);
            }
        }
    }

    /**
     * Subclasses define the specific interfaces (extending B) they support.
     */
    protected abstract List<Class<? extends B>> getAllowedListeners();
}
