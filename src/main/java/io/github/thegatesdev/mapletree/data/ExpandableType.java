package io.github.thegatesdev.mapletree.data;

import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ExpandableType<T> implements DataType<T>, ReadableDataHolder {

    private final List<Expansion<?>> expansions = new ArrayList<>();
    private final Function<DataMap, T> baseCreator;
    private final ReadableData readableData;

    private final DataType<List<T>> listType = Readable.createList(this);
    private String id;

    public ExpandableType(Class<T> type, ReadableData readableData, Function<DataMap, T> baseCreator) {
        this.baseCreator = baseCreator;
        this.readableData = readableData;
    }

    public <D> ExpandableType<T> expand(String key, DataTypeHolder<D> dataType, BiConsumer<D, T> action) {
        readableData.add(key, dataType, null);
        expansions.add(new Expansion<>(key, action));
        return this;
    }

    @Override
    public ReadableData getReadableData() {
        return readableData;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public T read(DataElement dataElement) {
        final DataMap data = readableData.read(dataElement.requireOf(DataMap.class));
        final T apply = baseCreator.apply(data);
        for (Expansion<?> expansion : expansions) {
            data.ifPrimitive(expansion.key, dataPrimitive -> expansion.action.accept(dataPrimitive.valueUnsafe(), apply));
        }
        return apply;
    }

    @Override
    public ExpandableType<T> id(final String id) {
        if (this.id == null) this.id = id;
        return this;
    }

    @Override
    public DataType<List<T>> listType() {
        return null;
    }

    private final class Expansion<D> {
        private final String key;
        private final BiConsumer<D, T> action;

        private Expansion(String key, BiConsumer<D, T> action) {
            this.key = key;
            this.action = action;
        }
    }
}