package io.github.thegatesdev.mapletree.registry;

import io.github.thegatesdev.mapletree.data.DataType;
import io.github.thegatesdev.mapletree.data.Factory;
import io.github.thegatesdev.mapletree.data.ReadableOptionsHolder;

import java.util.Collection;

public abstract class FactoryRegistry<Data, Fac extends Factory<? extends Data> & ReadableOptionsHolder> implements Identifiable, DataType<Data> {
    protected final String id;

    protected FactoryRegistry(String id) {
        this.id = id;
    }

    public abstract Collection<String> keys();

    public abstract Fac get(String key);

    @Override
    public String id() {
        return id;
    }
}
