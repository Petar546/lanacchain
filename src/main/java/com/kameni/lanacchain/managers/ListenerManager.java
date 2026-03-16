package com.kameni.lanacchain.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings("unchecked")
/**
 * @param <B> base listener interface which this manager handles
 *            example PeerNodeListener
 */
public abstract class ListenerManager<B> {
    private final Map<Class<? extends B>, List<B>> listenerGroups = new HashMap<>();

    /**
     * Internal helper to add to the specific group
     */
    private <L extends B> void addRawListener(Class<L> type, L listener) {
        List<L> group = (List<L>) listenerGroups.computeIfAbsent(type, _ -> new ArrayList<>());
        group.add(listener);
    }

    /**
     * checks if the listener matches any allowed sub-interfaces
     */
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
        listenerGroups.get(type).remove(listener);
    }

    public boolean hasListeners(Class<? extends B> type) {
        List<B> listeners = listenerGroups.get(type);
        return listeners != null && !listeners.isEmpty();
    }

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
