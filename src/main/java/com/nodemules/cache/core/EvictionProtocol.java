package com.nodemules.cache.core;

/**
 * @author brent
 * @since 7/29/18.
 */
@FunctionalInterface
public interface EvictionProtocol {

  void evict(long evictionSleepTime);
}
