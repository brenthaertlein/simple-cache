package com.nodemules.cache.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author brent
 * @since 7/29/18.
 */
@Getter
@AllArgsConstructor
public class RemovalEvent<K, V> {

  K id;
  V value;

}
