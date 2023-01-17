package io.github.thegatesdev.mapletree.data;

import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.mapletree.registry.Identifiable;

import java.util.List;

public interface DataType<D> extends DataTypeHolder<D>, Identifiable {

    D read(DataElement element);

    @Override
    default DataType<D> getDataType() {
        return this;
    }

    static <D> DataType<List<D>> list(DataTypeHolder<D> original) {
        return Readable.list(original);
    }
}
