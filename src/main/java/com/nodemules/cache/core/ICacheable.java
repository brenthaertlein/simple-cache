package com.nodemules.cache.core;

import java.io.Serializable;

/**
 * @author brent
 * @since 7/28/18.
 */
public interface ICacheable<K extends Serializable, V> {

  boolean isExpired();

  K getId();

  V getValue();

  void access();

}
