package io.github.thegatesdev.mapletree.registry.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class BasicRegistry<K, V> implements Registry<K, V> {
    private final Supplier<Map<K, V>> mapSupplier;
    private boolean isLocked = false;
    private Map<K, V> kvMap;
    private Map<K, V> kvMapView;

    private K prevK;
    private V prevV;

    private K[] keyArray;
    private boolean shouldRebuildKeyArray = true;

    public BasicRegistry() {
        this(HashMap::new);
    }

    public BasicRegistry(Supplier<Map<K, V>> mapSupplier) {
        this.mapSupplier = mapSupplier;
    }

    public void lock() {
        isLocked = true;
    }

    public void clear() {
        shouldRebuildKeyArray = true;
        kvMap = null;
        kvMapView = null;
        keyArray = null;
        prevK = null;
        prevV = null;
        isLocked = false;
    }

    public Map<K, V> back() {
        if (kvMapView == null) return Collections.emptyMap();
        return kvMapView;
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    public V register(K key, V value) {
        lockCheck();
        if (kvMap == null) {
            init();
            kvMap.put(key, value);
        } else {
            kvMap.putIfAbsent(key, value);
        }
        shouldRebuildKeyArray = true;
        return value;
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> function) {
        return kvMap.computeIfAbsent(key, function);
    }

    public void registerAll(Map<K, V> maps) {
        lockCheck();
        if (kvMap == null) {
            init();
            kvMap.putAll(maps);
        } else maps.forEach(kvMap::putIfAbsent);
        shouldRebuildKeyArray = true;
    }

    private void init() {
        if (kvMap == null) {
            kvMap = mapSupplier.get();
            kvMapView = Collections.unmodifiableMap(kvMap);
        }
    }

    public V get(K key) {
        if (kvMap == null || kvMap.isEmpty()) return null;
        if (prevK == key) return prevV;
        prevK = key;
        return prevV = kvMap.get(key);
    }

    public Set<K> keySet() {
        if (kvMapView == null) return Collections.emptySet();
        return kvMapView.keySet();
    }

    public K[] keyArray(K[] prep) {
        if (shouldRebuildKeyArray) {
            keyArray = keySet().toArray(prep);
            shouldRebuildKeyArray = false;
        }
        return keyArray;
    }
}
