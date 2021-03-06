package de.tuberlin.aura.core.dataflow.types;


public interface IMutableDataset<K,V> extends IDataset<V> {

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    abstract void put(final K key, final V value);

    abstract V get(final K key);
}
