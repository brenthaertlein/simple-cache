package com.nodemules.cache.core;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * @author brent
 * @since 7/28/18.
 */
@Slf4j
public abstract class Cache<K extends Serializable, V> {

  private static final Object LOCK = new Object();
  private final Map<K, ICacheable<K, V>> map = new ConcurrentHashMap<>();

  protected Cache() {
    log.debug("Cache()");
    Runnable r = () -> {
      log.debug("Running cleanup thread");
      final int sleepTimeMillis = 5000;
      try {
        while (true) {
          Thread.sleep(sleepTimeMillis);
          log.debug("Starting cleanup");
          Set<K> keys = map.keySet();
          int i = 0;
          for (K key : keys) {
            ICacheable<K, V> entry = map.get(key);
            if (entry.isExpired()) {
              log.debug("Removing record -> {}", key);
              map.remove(key);
              i++;
            }
          }
          log.debug("Ending cleanup: {} records removed this cycle", i);
          log.debug("Cleanup sleeping for {}ms", sleepTimeMillis);
        }

      } catch (InterruptedException e) {
      }
    };

    Thread t = new Thread(r);
    t.setPriority(Thread.MIN_PRIORITY);
    t.setDaemon(true);
    log.debug("Starting cleanup thread");
    t.start();
  }

  public K put(ICacheable<K, V> entry) {
    log.debug("Adding entry to cache -> {}", entry.getId());
    map.put(entry.getId(), entry);
    return entry.getId();
  }

  public V get(K key) {
    synchronized (LOCK) {
      log.debug("Getting entry from cache");
      ICacheable<K, V> entry = map.get(key);
      if (entry != null) {
        entry.access();
        if (entry.isExpired()) {
          log.debug("Removing expired entry on access -> {}", key);
          map.remove(key);
          return null;
        }
        log.debug("Returning entry from cache -> {}", entry);
        return entry.getValue();
      }
      return null;
    }
  }

}
