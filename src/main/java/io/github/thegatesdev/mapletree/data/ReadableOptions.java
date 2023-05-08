package io.github.thegatesdev.mapletree.data;

import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.data.DataNull;
import io.github.thegatesdev.maple.data.DataPrimitive;
import io.github.thegatesdev.maple.exception.ElementException;

import java.util.*;
import java.util.function.Function;

public class ReadableOptions {

    private final Object MUT_ENTRIES = new Object(), MUT_AFTER = new Object();
    protected Map<String, Entry<?>> entries;
    protected Map<String, Function<DataMap, DataPrimitive>> afterFunctions;

    public DataMap read(DataMap data) {
        // Create output
        final DataMap output = new DataMap();
        try {
            if (entries != null) synchronized (MUT_ENTRIES) {
                entries.forEach((key, value) -> {
                    final DataElement read = readEntry(value, data.getOrNull(key));
                    if (read == null) throw ElementException.requireField(data, key);
                    output.put(key, read);
                });
            }
            // afterFunctions allow for some calculations ( e.g. generate a predicate for multiple conditions )
            if (afterFunctions != null) synchronized (MUT_AFTER) {
                afterFunctions.forEach((key, value) -> output.put(key, value.apply(output)));
            }
        } catch (ElementException e) {
            throw e;
        } catch (Throwable e) {
            throw new ElementException(data, "readableData error; %s".formatted(e.getMessage()), e);
        }
        return output;
    }

    private DataElement readEntry(Entry<?> value, DataElement element) {
        if (element == null) { // Not present
            if (value.hasDefault) {
                if (value.defaultValue == null) return new DataNull(); // Default null value
                else return new DataPrimitive(value.defaultValue); // Has default
            } else return null; // Not present and no default is error
        } else return new DataPrimitive(value.dataType.read(element)); // Present
    }

    // --

    public ReadableOptions add(String key, DataTypeHolder<?> holder) {
        return add(key, new Entry<>(holder.dataType()));
    }

    public <T> ReadableOptions add(String key, DataTypeHolder<T> holder, T defaultValue) {
        return add(key, new Entry<>(holder.dataType(), defaultValue));
    }

    public <T> ReadableOptions add(DataTypeHolder<T> holder, Map<String, T> values) {
        values.forEach((s, t) -> this.add(s, holder, t));
        return this;
    }

    public <T> ReadableOptions add(List<String> values, DataTypeHolder<T> holder, T def) {
        values.forEach(s -> add(s, holder, def));
        return this;
    }

    public ReadableOptions add(List<String> values, DataTypeHolder<?> holder) {
        values.forEach(s -> this.add(s, holder));
        return this;
    }

    protected ReadableOptions add(String key, Entry<?> entry) {
        if (entries == null) entries = new LinkedHashMap<>();
        entries.putIfAbsent(key, entry);
        return this;
    }


    public ReadableOptions after(String s, Function<DataMap, DataPrimitive> function) {
        if (afterFunctions == null) afterFunctions = new TreeMap<>();
        afterFunctions.putIfAbsent(s, function);
        return this;
    }

    // --

    private static StringBuilder displayEntry(Entry<?> entry) {
        final StringBuilder builder = new StringBuilder("A " + entry.id() + "; ");
        if (entry.hasDefault) {
            if (entry.defaultValue == null) builder.append("optional");
            else builder.append("default value: ").append(entry.defaultValue);
        } else builder.append("required");
        return builder;
    }

    public String displayEntries() {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<String, Entry<?>> entry : entries.entrySet())
            builder.append(entry.getKey()).append(": ").append(displayEntry(entry.getValue())).append("\n");
        return builder.toString();
    }

    public Map<String, Entry<?>> getEntries() {
        if (entries == null) return Collections.emptyMap();
        return Collections.unmodifiableMap(entries);
    }

    public static class Entry<T> implements DataTypeHolder<T> {
        protected final DataType<T> dataType;
        protected final T defaultValue;
        protected final boolean hasDefault;

        private Entry(DataType<T> dataType) {
            this.dataType = dataType;
            this.defaultValue = null;
            hasDefault = false;
        }

        private Entry(DataType<T> dataType, T defaultValue) {
            this.dataType = dataType;
            this.defaultValue = defaultValue;
            hasDefault = true;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public boolean hasDefault() {
            return hasDefault;
        }

        public DataType<T> dataType() {
            return dataType;
        }
    }
}
