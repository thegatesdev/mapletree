package io.github.thegatesdev.mapletree.factory;

import io.github.thegatesdev.maple.data.DataMap;

public interface Factory<G> {

    G build(DataMap data);
}
