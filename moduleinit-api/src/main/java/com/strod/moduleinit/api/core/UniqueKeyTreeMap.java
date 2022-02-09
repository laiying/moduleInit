package com.strod.moduleinit.api.core;

import java.util.TreeMap;

public class UniqueKeyTreeMap<K, V> extends TreeMap<K, V> {
    private String tipText;

    public UniqueKeyTreeMap(String exceptionText) {
        super();

        tipText = exceptionText;
    }

    @Override
    public V put(K key, V value) {
        if (containsKey(key)) {
            throw new RuntimeException(String.format(tipText, key));
        } else {
            return super.put(key, value);
        }
    }
}
