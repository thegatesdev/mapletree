package io.github.thegatesdev.mapletree.registry.core;

import java.util.Map;
import java.util.Set;

public interface Registry<K, V> extends Lockable {
    V register(K k, V v);

    void registerAll(Map<K, V> map);

    V get(K key);

    Set<K> keySet();

    void clear();
}
