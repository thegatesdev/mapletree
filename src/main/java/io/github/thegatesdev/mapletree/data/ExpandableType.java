package io.github.thegatesdev.mapletree.data;

import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ExpandableType<T> extends DataType<T> implements ReadableDataHolder {

    private final List<Reader<?>> readers = new ArrayList<>();
    private final Function<DataMap, T> dataType;
    private final ReadableData readableData;

    public ExpandableType(Class<T> type, ReadableData readableData, Function<DataMap, T> baseCreator) {
        super(type);
        this.dataType = baseCreator;
        this.readableData = readableData;
    }

    public <D> void addReader(String key, DataTypeHolder<D> dataType, BiConsumer<D, T> action) {
        readableData.add(key, dataType, null);
        readers.add(new Reader<>(key, action));
    }

    @Override
    public ReadableData getReadableData() {
        return readableData;
    }

    @Override
    public T read(DataElement dataElement) {
        final DataMap data = readableData.read(dataElement.requireOf(DataMap.class));
        final T apply = dataType.apply(data);
        for (Reader<?> reader : readers) {
            data.ifPrimitive(reader.key, dataPrimitive -> reader.action.accept(dataPrimitive.valueUnsafe(), apply));
        }
        return apply;
    }

    @Override
    public ExpandableType<T> id(String identifier) {
        super.id(identifier);
        return this;
    }

    private final class Reader<D> {
        private final String key;
        private final BiConsumer<D, T> action;

        private Reader(String key, BiConsumer<D, T> action) {
            this.key = key;
            this.action = action;
        }
    }
}
