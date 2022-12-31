package io.github.thegatesdev.mapletree.registry;

public interface Identifiable {
    String id();

    default String friendlyId() {
        final String id = id();
        return id == null ? "unknown" : id;
    }
}
