package org.leibnizcenter.cfg.util;

import java.util.Map;

public class MapEntry<K, V> implements Map.Entry<K, V> {
    private final K key;
    private final V value;

  public MapEntry(final K key, final V value) {
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
  public final V setValue(final V value) {
    throw new UnsupportedOperationException();
  }
}
