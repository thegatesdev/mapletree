package io.github.thegatesdev.mapletree.data;

import io.github.thegatesdev.mapletree.registry.Identifiable;

import java.util.List;

public interface DataTypeHolder<D> extends Identifiable {
    DataType<D> dataType();

    default DataType<List<D>> list() {
        return dataType().list();
    }

    default String id() {
        return dataType().id();
    }
}
