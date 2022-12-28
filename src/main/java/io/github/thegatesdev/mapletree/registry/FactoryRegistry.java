package io.github.thegatesdev.mapletree.registry;

import io.github.thegatesdev.maple.exception.ElementException;
import io.github.thegatesdev.mapletree.data.DataType;
import io.github.thegatesdev.mapletree.data.DataTypeHolder;
import io.github.thegatesdev.mapletree.data.Readable;
import io.github.thegatesdev.mapletree.factory.Factory;
import io.github.thegatesdev.mapletree.registry.core.BasicRegistry;
import io.github.thegatesdev.mapletree.registry.core.Registry;

import java.util.Collection;
import java.util.function.Function;

// Factory registry = registry of factories and immediately statically register some.
public abstract class FactoryRegistry<D, F extends Factory<D>> extends BasicRegistry<String, Factory<D>> implements Identifiable, DataTypeHolder<D> {
    protected final String id;
    private final Function<F, String> keyGetter;
    private final DataType<D> dataType;
    private int registered = 0;

    protected FactoryRegistry(String id, Function<F, String> keyGetter) {
        this.id = id;
        this.keyGetter = keyGetter;
        this.dataType = registryDatatype(id, this);
    }

    private static <D> DataType<D> registryDatatype(String id, Registry<String, Factory<D>> registry) {
        return Readable.map(data -> {
            final String s = data.getString("type");
            final Factory<D> factory = registry.get(s);
            if (factory == null)
                throw new ElementException(data, "specified %s type %s does not exist".formatted(id, s));
            return factory.build(data);
        }).id(id);
    }

    public abstract void registerStatic();

    public void register(F factory) {
        register(keyGetter.apply(factory), factory);
    }

    public void registerAll(Collection<F> factories) {
        for (final F factory : factories) register(factory);
    }

    @SafeVarargs // We don't let the reference escape, and we don't store anything in the array.
    public final void registerAll(F... factories) {
        for (final F factory : factories) {
            register(factory);
        }
    }

    public final int registered() {
        return registered;
    }

    @Override
    public DataType<D> getDataType() {
        return dataType;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Factory<D> register(final String key, final Factory<D> value) {
        registered++;
        return super.register(key, value);
    }
}

