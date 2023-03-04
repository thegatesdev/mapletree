package io.github.thegatesdev.mapletree.registry;

import io.github.thegatesdev.mapletree.data.DataType;

import java.util.*;

public class DataTypeInfo<D, T extends DataType<D>> {

    private final T dataType;
    private List<String> description;

    private DataTypeInfo(T dataType) {
        this.dataType = dataType;
    }

    public List<String> description() {
        return description;
    }

    public DataTypeInfo<D, T> description(final String... description) {
        if (this.description == null) this.description = new ArrayList<>(description.length);
        Collections.addAll(this.description, description);
        return this;
    }

    public T dataType() {
        return dataType;
    }

    // -- STATIC

    private static final Map<String, DataTypeInfo<?, ?>> DATA_TYPE_RECORD = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <D, T extends DataType<D>> DataTypeInfo<D, T> get(T dataType) {
        return (DataTypeInfo<D, T>) DATA_TYPE_RECORD.computeIfAbsent(dataType.id(), s -> new DataTypeInfo<>(dataType));
    }

    public static Set<String> keys() {
        return DATA_TYPE_RECORD.keySet();
    }

    public static Collection<DataTypeInfo<?, ?>> values() {
        return DATA_TYPE_RECORD.values();
    }
    
    public static DataTypeInfo<?, ?> get(String key) {
        return DATA_TYPE_RECORD.get(key);
    }
}
