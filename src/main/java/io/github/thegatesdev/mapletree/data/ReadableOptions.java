package io.github.thegatesdev.mapletree.data;

import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.data.DataNull;
import io.github.thegatesdev.maple.data.DataPrimitive;
import io.github.thegatesdev.maple.exception.ElementException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ReadableOptions {

    private final Object MUT_ENTRIES = new Object(), MUT_AFTER = new Object();
    protected List<OptionEntry<?>> entries;
    protected List<AfterEntry> afterFunctions;

    public DataMap read(DataMap data) {
        // Create output
        final DataMap output = new DataMap();
        try {
            if (entries != null) synchronized (MUT_ENTRIES) {
                for (OptionEntry<?> entry : entries) {
                    final DataElement read = readEntry(entry, data.getOrNull(entry.key));
                    if (read == null) throw ElementException.requireField(data, entry.key);
                    output.put(entry.key, read);
                }
            }
            // afterFunctions allow for some calculations ( e.g. generate a predicate for multiple conditions )
            if (afterFunctions != null) synchronized (MUT_AFTER) {
                afterFunctions.forEach((value) -> output.put(value.key, value.modifier.apply(output)));
            }
        } catch (ElementException e) {
            throw e;
        } catch (Exception e) {
            throw new ElementException(data, "readableData error; %s".formatted(e.getMessage()), e);
        }
        return output;
    }

    private DataElement readEntry(OptionEntry<?> value, DataElement element) {
        if (element == null) { // Not present
            if (value.hasDefault) {
                if (value.defaultValue == null) return new DataNull(); // Default null value
                else return new DataPrimitive(value.defaultValue); // Has default
            } else return null; // Not present and no default is error
        } else return new DataPrimitive(value.dataType.read(element)); // Present
    }

    // --

    public ReadableOptions add(String key, DataTypeHolder<?> holder) {
        return add(new OptionEntry<>(key, holder.dataType()));
    }

    public <T> ReadableOptions add(String key, DataTypeHolder<T> holder, T defaultValue) {
        return add(new OptionEntry<>(key, holder.dataType(), defaultValue));
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

    protected ReadableOptions add(OptionEntry<?> entry) {
        if (entries == null) entries = new ArrayList<>();
        entries.add(entry);
        return this;
    }


    public ReadableOptions after(String s, Function<DataMap, DataElement> function) {
        if (afterFunctions == null) afterFunctions = new ArrayList<>();
        afterFunctions.add(new AfterEntry(s, function));
        return this;
    }

    // --

    private static StringBuilder displayEntry(OptionEntry<?> entry) {
        final StringBuilder builder = new StringBuilder("A " + entry.id() + "; ");
        if (entry.hasDefault) {
            if (entry.defaultValue == null) builder.append("optional");
            else builder.append("default value: ").append(entry.defaultValue);
        } else builder.append("required");
        return builder;
    }

    public String displayEntries() {
        final StringBuilder builder = new StringBuilder();
        for (final OptionEntry<?> entry : entries)
            builder.append(entry.key).append(": ").append(displayEntry(entry)).append("\n");
        return builder.toString();
    }

    public List<OptionEntry<?>> entries() {
        return Collections.unmodifiableList(entries);
    }

    public record OptionEntry<T>(String key, DataType<T> dataType, T defaultValue,
                                 boolean hasDefault) implements DataTypeHolder<T> {
        public OptionEntry(String key, DataType<T> dataType, T defaultValue) {
            this(key, dataType, defaultValue, true);
        }

        public OptionEntry(String key, DataType<T> dataType) {
            this(key, dataType, null, false);
        }
    }

    public record AfterEntry(String key, Function<DataMap, DataElement> modifier) {
    }
}
