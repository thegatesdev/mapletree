package io.github.thegatesdev.mapletree.registry;

import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.exception.ElementException;
import io.github.thegatesdev.mapletree.data.DataType;
import io.github.thegatesdev.mapletree.data.Factory;
import io.github.thegatesdev.mapletree.data.ReadableOptionsHolder;
import io.github.thegatesdev.mapletree.registry.core.BasicRegistry;

import java.util.Collection;
import java.util.function.Function;

public abstract class FactoryRegistry<D, F extends Factory<? extends D> & ReadableOptionsHolder> extends BasicRegistry<String, F> implements Identifiable, DataType<D> {
    protected final String id;
    private final Function<F, String> keyGetter;
    private int registered = 0;

    protected FactoryRegistry(String id, Function<F, String> keyGetter) {
        this.id = id;
        this.keyGetter = keyGetter;
    }

    public abstract void registerStatic();

    public void register(F factory) {
        register(keyGetter.apply(factory), factory);
    }

    public void registerAll(Collection<F> factories) {
        for (final F factory : factories) register(factory);
    }

    @SafeVarargs
    public final void registerAll(F... factories) {
        for (final F factory : factories) {
            register(factory);
        }
    }

    public final int registered() {
        return registered;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public D read(final DataElement element) {
        final DataMap data = element.requireOf(DataMap.class);
        final String s = data.getString("type");
        final Factory<? extends D> factory = get(s);
        if (factory == null)
            throw new ElementException(data, "specified %s %s does not exist".formatted(id, s));
        return factory.build(data);
    }

    @Override
    public <T extends F> T register(final String key, final T value) {
        registered++;
        return super.register(key, value);
    }
}

