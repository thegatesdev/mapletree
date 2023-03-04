package io.github.thegatesdev.mapletree.data;

import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataList;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.data.DataPrimitive;
import io.github.thegatesdev.maple.exception.ElementException;
import io.github.thegatesdev.mapletree.registry.DataTypeInfo;
import io.github.thegatesdev.threshold.Threshold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Readable<D> implements DataType<D> {
    private static final Map<String, Readable<?>> LIST_TYPES = new HashMap<>();

    private static final Map<Class<?>, Readable<?>> PRIMITIVE_CACHE = new HashMap<>();
    private final Class<D> dataClass;
    private final Function<DataElement, D> readFunction;
    private final String identifier;

    public Readable(String identifier, final Function<DataElement, D> readFunction) {
        this(identifier, null, readFunction);
    }

    public Readable(String identifier, final Class<D> dataClass, final Function<DataElement, D> readFunction) {
        this.identifier = identifier;
        this.dataClass = dataClass;
        this.readFunction = readFunction;
    }

    // --

    // CACHED TYPES

    @SuppressWarnings("unchecked")
    private static <D> Readable<D> getOrCreatePrimitive(Class<D> primitiveClass, Supplier<Readable<D>> supplier) {
        Readable<?> readable = PRIMITIVE_CACHE.get(primitiveClass);
        if (readable == null) {
            readable = supplier.get();
            PRIMITIVE_CACHE.put(primitiveClass, readable);
        }
        return (Readable<D>) readable;
    }

    @SuppressWarnings("unchecked")
    public static <D> Readable<List<D>> list(DataTypeHolder<D> original) {
        return (Readable<List<D>>) LIST_TYPES.computeIfAbsent(original.id(), k -> createList(original.dataType()));
    }

    private static <D> Readable<List<D>> createList(DataType<D> original) {
        return DataTypeInfo.get(new Readable<>(original.id() + "_list", element -> {
            final DataList list = element.requireOf(DataList.class);
            final List<D> results = new ArrayList<>(list.size());
            list.forEach(e -> results.add(original.read(e)));
            return results;
        })).description("List of '%s'".formatted(original.id())).dataType();
    }


    public static <D> Readable<D> primitive(Class<D> dataClass) {
        return primitive(dataClass.getSimpleName().toLowerCase(), dataClass);
    }

    public static <D> Readable<D> primitive(String identifier, Class<D> dataClass) {
        return getOrCreatePrimitive(dataClass, () -> DataTypeInfo.get(
                        new Readable<>(identifier, dataClass, element -> element.requireOf(DataPrimitive.class).requireValue(dataClass)))
                .description("A " + dataClass.getSimpleName()).dataType()
        );
    }

    public static <E extends Enum<E>> Readable<E> enumeration(Class<E> enumClass) {
        return getOrCreatePrimitive(enumClass, () -> DataTypeInfo.get(
                        single(enumClass.getSimpleName().toLowerCase(), enumClass, primitive -> {
                            final String s = primitive.stringValue();
                            final E enumValue = Threshold.enumGet(enumClass, s);
                            if (enumValue == null)
                                throw new ElementException(primitive, "'%s' does not contain value %s".formatted(enumClass.getSimpleName(), s));
                            return enumValue;
                        }))
                .description("A " + enumClass.getSimpleName(), "Possible values: " + String.join(", ", Threshold.enumNames(enumClass))).dataType());
    }

    // OTHER

    public static <D> Readable<D> single(String identifier, Class<D> dataClass, Function<DataPrimitive, D> primitiveReader) {
        return new Readable<>(identifier, dataClass, element -> primitiveReader.apply(element.requireOf(DataPrimitive.class)));
    }

    public static <D> Readable<D> map(String identifier, Class<D> dataClass, Function<DataMap, D> mapReader) {
        return new Readable<>(identifier, dataClass, element -> mapReader.apply(element.requireOf(DataMap.class)));
    }

    public static <D> Readable<D> map(String identifier, Function<DataMap, D> mapReader) {
        return map(identifier, (Class<D>) null, mapReader);
    }

    public static <D> Readable<D> map(String identifier, Class<D> dataClass, ReadableOptions readableOptions, Function<DataMap, D> mapReader) {
        return new Readable<>(identifier, dataClass, element -> mapReader.apply(readableOptions.read(element.requireOf(DataMap.class))));
    }

    public static <D> Readable<D> map(String identifier, ReadableOptions readableOptions, Function<DataMap, D> mapReader) {
        return map(identifier, null, readableOptions, mapReader);
    }

    @Override
    public String id() {
        return identifier;
    }

    @Override
    public D read(DataElement element) {
        try {
            return readFunction.apply(element);
        } catch (ElementException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new ElementException(element, "error happened while reading dataType " + friendlyId(), throwable);
        }
    }

    public Class<D> dataClass() {
        return dataClass;
    }
}
