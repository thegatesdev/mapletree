package io.github.thegatesdev.mapletree.registry;

import io.github.thegatesdev.mapletree.data.DataType;
import io.github.thegatesdev.mapletree.data.ReadableOptions;

import java.util.*;

public class DataTypeInfo<D, T extends DataType<D>> {

    private final T dataType;
    private String description, stringRep, origin;
    private List<String> possibleValues;
    private ReadableOptions readableOptions;

    private DataTypeInfo(T dataType) {
        this.dataType = dataType;
    }

    public T dataType() {
        return dataType;
    }

    // --

    public DataTypeInfo<D, T> description(final String description) {
        this.description = description;
        return this;
    }

    public DataTypeInfo<D, T> origin(final String origin) {
        this.origin = origin;
        return this;
    }

    public DataTypeInfo<D, T> representation(final String stringRep) {
        this.stringRep = stringRep;
        return this;
    }

    public DataTypeInfo<D, T> possibleValues(String... possibleValues) {
        if (this.possibleValues == null) this.possibleValues = new ArrayList<>(possibleValues.length);
        Collections.addAll(this.possibleValues, possibleValues);
        return this;
    }

    public DataTypeInfo<D, T> possibleValues(Collection<String> possibleValues) {
        if (this.possibleValues == null) this.possibleValues = new ArrayList<>(possibleValues.size());
        this.possibleValues.addAll(possibleValues);
        return this;
    }

    public DataTypeInfo<D, T> readableOptions(ReadableOptions readableOptions) {
        this.readableOptions = readableOptions;
        return this;
    }

    
    public ReadableOptions readableOptions() {
        return readableOptions;
    }

    public String description() {
        return description;
    }

    public String origin() {
        return origin;
    }

    public String representation() {
        return stringRep;
    }

    public List<String> possibleValues() {
        return possibleValues == null ? Collections.emptyList() : Collections.unmodifiableList(possibleValues);
    }

    // STATIC

    private static final Map<String, DataTypeInfo<?, ?>> INFO_RECORD = new HashMap<>();
    private static final Map<String, DataTypeInfo<?, ?>> VIEW = Collections.unmodifiableMap(INFO_RECORD);

    @SuppressWarnings("unchecked")
    public static <D, T extends DataType<D>> DataTypeInfo<D, T> get(T dataType) {
        return (DataTypeInfo<D, T>) INFO_RECORD.computeIfAbsent(dataType.id(), s -> new DataTypeInfo<>(dataType));
    }

    public static Set<String> keys() {
        return VIEW.keySet();
    }

    public static Collection<DataTypeInfo<?, ?>> values() {
        return VIEW.values();
    }

    public static DataTypeInfo<?, ?> get(String key) {
        return INFO_RECORD.get(key);
    }
}
