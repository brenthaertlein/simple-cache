package com.nodemules.cache.core;

import java.io.Serializable;

/**
 * @author brent
 * @since 7/29/18.
 */
public interface RemovalListener<K extends Serializable, V> {

  void onRemoval(RemovalEvent<K, V> event);
}
