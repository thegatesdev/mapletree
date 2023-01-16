package io.github.thegatesdev.mapletree.registry.core;

import java.util.Map;
import java.util.Set;

public interface Registry<K, V> extends Lockable {
    <T extends V> T register(K k, T v);

    void registerAll(Map<K, V> map);

    V get(K key);

    Set<K> keySet();

    void clear();
}
