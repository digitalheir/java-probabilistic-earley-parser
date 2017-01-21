package org.leibnizcenter.cfg.util;

import java.util.Map;

public class MapEntry<K, V> implements Map.Entry<K, V> {
  final K key;
  final V value;

  public MapEntry(K key, V value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public final K getKey() {
    return key;
  }

  @Override
  public final V getValue() {
    return value;
  }

  @Override
  public final V setValue(V value) {
    throw new UnsupportedOperationException();
  }
}
