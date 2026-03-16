package com.kameni.lanacchain.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
@SuppressWarnings("unchecked")
public class ListenerManager {
    private final Map<Class<?>, List<?>> listenerGroups = new HashMap<>();


    public <L> void addListener(Class<L> type, L listener) {
        (
            (List<L>) listenerGroups.computeIfAbsent(type, _ -> new ArrayList<L>())
        ).add(listener);
    }

    public <L> void removeListener(Class<L> type, L listener) {
        (
            (List<L>) listenerGroups.get(type)
        ).remove(listener);
    }

    public boolean hasListeners(Class<?> type) {
        List<?> listeners = listenerGroups.get(type);
        return listeners != null && !listeners.isEmpty();
    }

    public <L, T> void notifyAll(Class<L> type, BiConsumer<L, T> method, T arguments) {
        List<L> listeners = (List<L>) listenerGroups.get(type);
        if (listeners != null) {
            for (L listener : listeners) {
                method.accept(listener, arguments);
            }
        }
    }
}
