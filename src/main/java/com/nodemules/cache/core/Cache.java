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
  private static final long DEFAULT_EVICTION_SLEEP_TIME = 5000L;

  private final Map<K, CachedRecord<K, V>> map = new ConcurrentHashMap<>();

  private final Long ttl;
  private final boolean refreshTtl;

  private final Long evictionSleepTime;
  private final EvictionProtocol evictionProtocol;
  private final EvictionProtocol defaultEvictionProtocol = sleepTime -> {
    log.trace("Running cleanup thread");
    try {
      while (true) {
        Thread.sleep(sleepTime);
        log.trace("Starting cleanup");
        Set<K> keys = map.keySet();
        int i = 0;
        for (K key : keys) {
          ICacheable<K, V> entry = map.get(key);
          if (entry.isExpired()) {
            log.trace("Record created at -> {}", entry.getCreatedTime());
            log.trace("Record expired at -> {}", entry.getExpireTime());
            remove(key);
            i++;
          }
        }
        log.trace("Ending cleanup: {} records removed this cycle", i);
        log.trace("Cleanup sleeping for {}ms", sleepTime);
      }

    } catch (InterruptedException e) {
    }
  };

  protected Cache() {
    throw new AssertionError("Use Cache.CacheBuilder()");
  }

  private Cache(Long ttl, boolean refreshTtlOnAccess, Long evictionSleepTime,
      EvictionProtocol evictionProtocol) {
    log.trace("Cache()");
    this.ttl = ttl;
    this.refreshTtl = refreshTtlOnAccess;
    if (evictionSleepTime == null) {
      this.evictionSleepTime = DEFAULT_EVICTION_SLEEP_TIME;
    } else {
      this.evictionSleepTime = evictionSleepTime;
    }
    if (evictionProtocol == null) {
      this.evictionProtocol = defaultEvictionProtocol;
    } else {
      this.evictionProtocol = evictionProtocol;
    }
    Thread t = new Thread(() -> this.evictionProtocol.evict(this.evictionSleepTime));
    t.setPriority(Thread.MIN_PRIORITY);
    t.setDaemon(true);
    log.trace("Starting cleanup thread");
    t.start();
  }

  public static Cache.CacheBuilder builder() {
    return new Cache.CacheBuilder();
  }

  private void remove(K key) {
    log.trace("Removing record -> {}", key);
    map.remove(key);
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

  public static class CacheBuilder {

    private Long ttl;
    private Long evictionSleepTime;
    private boolean refreshTtlOnAccess;
    private EvictionProtocol evictionProtocol;

    CacheBuilder() {
    }

    public CacheBuilder ttl(long ttl) {
      this.ttl = ttl;
      return this;
    }

    public CacheBuilder refreshTtlOnAccess(boolean refreshTtlOnAccess) {
      this.refreshTtlOnAccess = refreshTtlOnAccess;
      return this;
    }

    public CacheBuilder evictionProtocol(EvictionProtocol evictionProtocol) {
      this.evictionProtocol = evictionProtocol;
      return this;
    }

    public CacheBuilder evictionSleepTime(long evictionSleepTime) {
      this.evictionSleepTime = evictionSleepTime;
      return this;
    }

    public <K extends Serializable, V> Cache<K, V> build() {
      return new Cache<K, V>(ttl, refreshTtlOnAccess, evictionSleepTime, evictionProtocol) {
      };
    }
  }

}
