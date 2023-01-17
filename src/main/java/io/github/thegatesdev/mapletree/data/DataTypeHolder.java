package io.github.thegatesdev.mapletree.data;

import java.util.List;

public interface DataTypeHolder<D> {
    DataType<D> getDataType();

    default DataType<List<D>> list() {
        return getDataType().list();
    }
}
