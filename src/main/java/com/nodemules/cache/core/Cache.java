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
  private final Map<K, CachedRecord<K, V>> map = new ConcurrentHashMap<>();
  private final Long ttl;
  private final boolean refreshTtl;

  protected Cache(Long ttl, boolean refreshTtlOnAccess) {
    log.trace("Cache()");
    this.ttl = ttl;
    this.refreshTtl = refreshTtlOnAccess;
    Runnable r = () -> {
      log.trace("Running cleanup thread");
      final int sleepTimeMillis = 1000;
      try {
        while (true) {
          Thread.sleep(sleepTimeMillis);
          log.trace("Starting cleanup");
          Set<K> keys = map.keySet();
          int i = 0;
          for (K key : keys) {
            ICacheable<K, V> entry = map.get(key);
            if (entry.isExpired()) {
              log.trace("Record created at -> {}", entry.getCreatedTime());
              log.trace("Record expired at -> {}", entry.getExpireTime());
              log.trace("Removing record -> {}", key);
              map.remove(key);
              i++;
            }
          }
          log.trace("Ending cleanup: {} records removed this cycle", i);
          log.trace("Cleanup sleeping for {}ms", sleepTimeMillis);
        }

      } catch (InterruptedException e) {
      }
    };

    Thread t = new Thread(r);
    t.setPriority(Thread.MIN_PRIORITY);
    t.setDaemon(true);
    log.trace("Starting cleanup thread");
    t.start();
  }

  protected Cache(Long ttl) {
    this(ttl, false);
  }

  protected Cache() {
    this(null, false);
  }

  public K put(CachedRecord<K, V> entry) {
    log.trace("Adding entry to cache -> {}", entry.getId());
    log.trace("Custom cache ttl -> {}", ttl);
    log.trace("Entry ttl -> {}", entry.getTtl());
    if (ttl != null && entry.getTtl() == null) {
      entry.setExpireTime(ttl, refreshTtl);
    }
    map.put(entry.getId(), entry);
    return entry.getId();
  }

  public V get(K key) {
    synchronized (LOCK) {
      log.trace("Getting entry from cache");
      CachedRecord<K, V> entry = map.get(key);
      if (entry != null) {
        entry.access();
        if (entry.isExpired()) {
          log.trace("Removing expired entry on access -> {}", key);
          map.remove(key);
          return null;
        }
        log.trace("Returning entry from cache -> {}", entry);
        return entry.getValue();
      }
      return null;
    }
  }

}
