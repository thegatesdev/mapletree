package io.github.thegatesdev.mapletree.data;

import java.util.List;

public interface DataTypeHolder<D> {
    DataType<D> getDataType();

    // listType proxy
    default DataType<List<D>> listType() {
        return getDataType().listType();
    }
}
