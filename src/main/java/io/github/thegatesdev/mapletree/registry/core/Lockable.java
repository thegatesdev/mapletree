package io.github.thegatesdev.mapletree.registry.core;

public interface Lockable {
    void lock();

    boolean isLocked();

    default void lockCheck() {
        if (isLocked()) throw new RuntimeException("locked");
    }
}
