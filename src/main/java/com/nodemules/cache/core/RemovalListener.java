package com.nodemules.cache.core;

import java.io.Serializable;

/**
 * @author brent
 * @since 7/29/18.
 */
public class RemovalListener<K extends Serializable, V> {

  public void onRemoval(RemovalEvent<K, V> event) {

  }
}
