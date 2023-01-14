package io.github.thegatesdev.mapletree.data;

import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataList;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.data.DataPrimitive;
import io.github.thegatesdev.maple.exception.ElementException;
import io.github.thegatesdev.threshold.Threshold;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class Readable<D> implements DataType<D> {

    private static final List<Readable<?>> ALL = new ArrayList<>();
    private static final List<Readable<?>> ALL_VIEW = Collections.unmodifiableList(ALL);
    private static final Map<Class<?>, Readable<?>> PRIMITIVE_CACHE = new HashMap<>();
    private final Class<D> dataClass;
    private final Function<DataElement, D> readFunction;
    private String identifier;
    // CACHE
    private Readable<List<D>> listType;

    public Readable(final Function<DataElement, D> readFunction) {
        this(null, readFunction);
    }

    public Readable(final Class<D> dataClass, final Function<DataElement, D> readFunction) {
        this.dataClass = dataClass;
        this.readFunction = readFunction;
        ALL.add(this);
    }

    public static List<Readable<?>> getAll() {
        return ALL_VIEW;
    }

    // --

    @SuppressWarnings("unchecked")
    private static <D> Readable<D> getOrCreatePrimitive(Class<D> primitiveClass, Supplier<Readable<D>> supplier) {
        Readable<?> readable = PRIMITIVE_CACHE.get(primitiveClass);
        if (readable == null) {
            readable = supplier.get();
            PRIMITIVE_CACHE.put(primitiveClass, readable);
        }
        return (Readable<D>) readable;
    }

    protected static <D> Readable<List<D>> createList(DataType<D> original) {
        return new Readable<>(null, element -> {
            final DataList list = element.requireOf(DataList.class);
            final List<D> results = new ArrayList<>(list.size());
            list.forEach(e -> results.add(original.read(e)));
            return results;
        });
    }

    public static <D> Readable<D> primitive(Class<D> dataClass) {
        return getOrCreatePrimitive(dataClass, () -> new Readable<>(dataClass, element -> element.requireOf(DataPrimitive.class).requireValue(dataClass))).id(dataClass.getSimpleName().toLowerCase());
    }

    public static <D> Readable<D> primitive(Class<D> dataClass, Function<DataPrimitive, D> primitiveReader) {
        return new Readable<>(dataClass, element -> primitiveReader.apply(element.requireOf(DataPrimitive.class)));
    }

    public static <E extends Enum<E>> Readable<E> enumeration(Class<E> enumClass) {
        return getOrCreatePrimitive(enumClass, () -> primitive(enumClass, primitive -> {
            final String s = primitive.stringValue();
            final E enumValue = Threshold.enumGet(enumClass, s);
            if (enumValue == null)
                throw new ElementException(primitive, "'%s' does not contain value %s".formatted(enumClass.getSimpleName(), s));
            return enumValue;
        })).id(enumClass.getSimpleName().toLowerCase());
    }

    public static <D> Readable<D> map(Class<D> dataClass, Function<DataMap, D> mapReader) {
        return new Readable<>(dataClass, element -> mapReader.apply(element.requireOf(DataMap.class)));
    }

    public static <D> Readable<D> map(Function<DataMap, D> mapReader) {
        return map((Class<D>) null, mapReader);
    }

    public static <D> Readable<D> map(Class<D> dataClass, ReadableData readableData, Function<DataMap, D> mapReader) {
        return new Readable<>(dataClass, element -> mapReader.apply(readableData.read(element.requireOf(DataMap.class))));
    }

    public static <D> Readable<D> map(ReadableData readableData, Function<DataMap, D> mapReader) {
        return map(null, readableData, mapReader);
    }

    public Readable<D> id(final String identifier) {
        this.identifier = identifier;
        return this;
    }

    public Class<D> dataClass() {
        return dataClass;
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

    @Override
    public Readable<List<D>> listType() {
        if (listType == null) listType = createList(this);
        return listType;
    }
}
