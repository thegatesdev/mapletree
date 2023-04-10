package io.github.thegatesdev.mapletree.registry;

import io.github.thegatesdev.mapletree.data.DataType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataTypeInfo<D, T extends DataType<D>> {

    private final T dataType;
    private String description, stringRep, origin;

    private DataTypeInfo(T dataType) {
        this.dataType = dataType;
    }

    public T dataType() {
        return dataType;
    }

    // --

    public String description() {
        return description;
    }

    public DataTypeInfo<D, T> description(final String description) {
        this.description = description;
        return this;
    }

    public String representation() {
        return stringRep;
    }

    public DataTypeInfo<D, T> representation(final String stringRep) {
        this.stringRep = stringRep;
        return this;
    }

    public DataTypeInfo<D, T> origin(final String origin) {
        this.origin = origin;
        return this;
    }

    public String origin() {
        return origin;
    }

    // STATIC

    private static final Map<String, DataTypeInfo<?, ?>> INFO_RECORD = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <D, T extends DataType<D>> DataTypeInfo<D, T> get(T dataType) {
        return (DataTypeInfo<D, T>) INFO_RECORD.computeIfAbsent(dataType.id(), s -> new DataTypeInfo<>(dataType));
    }

    public static Set<String> keys() {
        return INFO_RECORD.keySet();
    }

    public static Collection<DataTypeInfo<?, ?>> values() {
        return INFO_RECORD.values();
    }

    public static DataTypeInfo<?, ?> get(String key) {
        return INFO_RECORD.get(key);
    }
}
