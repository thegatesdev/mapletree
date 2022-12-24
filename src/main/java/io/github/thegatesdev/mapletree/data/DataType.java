package io.github.thegatesdev.mapletree.data;

import io.github.thegatesdev.maple.data.DataElement;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class DataType<D> implements DataTypeHolder<D> {
    private static final Map<String, DataType<?>> DATA_TYPES = new LinkedHashMap<>();

    private final Class<D> dataClass;
    private String identifier;

    private Readable<List<D>> listType;

    public DataType(Class<D> dataClass) {
        this.dataClass = dataClass;
    }

    public DataType(Class<D> dataClass, String id) {
        this.dataClass = dataClass;
        id(id);
    }


    public static Map<String, DataType<?>> all() {
        return Collections.unmodifiableMap(DATA_TYPES);
    }

    // --

    @Override
    public Readable<List<D>> listType() {
        if (listType == null) {
            listType = Readable.createList(this);
        }
        return listType;
    }

    public abstract D read(DataElement element);

    // --

    public DataType<D> id(String identifier) {
        if (this.identifier == null) {
            if (DATA_TYPES.containsKey(identifier))
                throw new IllegalArgumentException("Duplicate DataType identifier " + identifier);
            this.identifier = identifier;
            DATA_TYPES.put(identifier, this);
        }
        return this;
    }

    public String id() {
        return identifier;
    }


    public String friendlyId() {
        return identifier != null ? identifier : "unknown";
    }

    public Class<D> dataClass() {
        return dataClass;
    }

    @Override
    public DataType<D> getDataType() {
        return this;
    }
}
