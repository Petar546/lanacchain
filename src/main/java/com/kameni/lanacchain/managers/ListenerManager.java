package com.kameni.lanacchain.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ListenerManager<L> {
    private final List<L> listeners = new ArrayList<>();

    public void addListener(L listener) {
        listeners.add(listener);
    }

    public void removeListener(L listener) {
        listeners.remove(listener);
    }

    public boolean hasListeners(){
        return !listeners.isEmpty();
    }

    public  <T> void notifyAll(BiConsumer<L, T> method, T arguments) {
        for (L listener : listeners) {
            method.accept(listener, arguments);
        }
    }
}
