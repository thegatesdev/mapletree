package io.github.thegatesdev.mapletree.data;

import io.github.thegatesdev.mapletree.registry.Identifiable;

import java.util.List;

@FunctionalInterface
public interface DataTypeHolder<D> extends Identifiable {
    default DataType<List<D>> list() {
        return dataType().list();
    }

    DataType<D> dataType();

    default String id() {
        return dataType().id();
    }
}
