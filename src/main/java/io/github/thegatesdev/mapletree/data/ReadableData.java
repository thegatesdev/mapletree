package io.github.thegatesdev.mapletree.data;

import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.data.DataNull;
import io.github.thegatesdev.maple.data.DataPrimitive;
import io.github.thegatesdev.maple.exception.ElementException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ReadableData {

    protected Map<String, Entry<?>> dataTypeMap;
    protected Map<String, Function<DataMap, DataPrimitive>> afterFunctions;

    public ReadableData() {
    }

    public Map<String, Entry<?>> getEntries() {
        if (dataTypeMap == null) return Collections.emptyMap();
        return Collections.unmodifiableMap(dataTypeMap);
    }

    public DataMap read(DataMap data) {
        if (isEmpty())
            return new DataMap();
        // Create output
        final DataMap output = new DataMap(dataTypeMap == null ? 0 : dataTypeMap.size() + (afterFunctions == null ? 0 : afterFunctions.size()));
        try {
            if (dataTypeMap != null && !dataTypeMap.isEmpty()) {
                dataTypeMap.forEach((key, value) -> {
                    final DataElement element = data.getOrNull(key);
                    if (element == null) { // Not present
                        if (value.hasDefault) {
                            if (value.defaultValue == null) output.put(key, new DataNull()); // Default null value
                            else output.put(key, new DataPrimitive(value.defaultValue)); // Has default
                        } else throw ElementException.requireField(data, key); // Not present and no default is error
                    } else output.put(key, new DataPrimitive(value.dataType.read(element))); // Present
                });
            }
            // afterFunctions allow for some pre-calculations ( e.g. generate a predicate for multiple conditions )
            if (afterFunctions != null && !afterFunctions.isEmpty()) {
                afterFunctions.forEach((key, value) -> output.put(key, value.apply(output)));
            }
        } catch (ElementException e) {
            throw e;
        } catch (Throwable e) {
            throw new ElementException(data, "readableData error; %s".formatted(e.getMessage()), e);
        }
        return output;
    }

    public ReadableData add(String key, DataTypeHolder<?> holder) {
        return add(key, new Entry<>(holder.getDataType()));
    }

    public <T> ReadableData add(String key, DataTypeHolder<T> holder, T defaultValue) {
        return add(key, new Entry<>(holder.getDataType(), defaultValue));
    }

    // --

    public <T> ReadableData add(DataTypeHolder<T> holder, Map<String, T> values) {
        values.forEach((s, t) -> this.add(s, holder, t));
        return this;
    }

    // -

    public <T> ReadableData add(List<String> values, DataTypeHolder<T> holder, T def) {
        values.forEach(s -> add(s, holder, def));
        return this;
    }

    public <T> ReadableData add(List<String> values, DataTypeHolder<T> holder) {
        values.forEach(s -> this.add(s, holder));
        return this;
    }

    // -

    public ReadableData after(String s, Function<DataMap, DataPrimitive> function) {
        if (afterFunctions == null) initAfterFunctions();
        afterFunctions.putIfAbsent(s, function);
        return this;
    }

    public boolean isEmpty() {
        return dataTypeMap == null || dataTypeMap.isEmpty() && (afterFunctions == null || afterFunctions.isEmpty());
    }

    protected void initDataTypeMap() {
        dataTypeMap = new LinkedHashMap<>(1);
    }

    protected void initAfterFunctions() {
        afterFunctions = new LinkedHashMap<>(1);
    }

    // --

    protected ReadableData add(String key, Entry<?> entry) {
        if (dataTypeMap == null) initDataTypeMap();
        dataTypeMap.putIfAbsent(key, entry);
        return this;
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

        public DataType<T> getDataType() {
            return dataType;
        }
    }
}
