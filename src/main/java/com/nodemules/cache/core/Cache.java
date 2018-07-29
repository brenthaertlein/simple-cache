package com.nodemules.cache.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
  private final List<RemovalListener<K, V>> removalListeners = new ArrayList<>();
  private final Long ttl;
  private final boolean refreshTtl;
  private final Long evictionSleepTime;
  private final EvictionProtocol evictionProtocol;
  private final EvictionProtocol defaultEvictionProtocol = sleepTime -> {
    try {
      log.trace("Running cleanup thread with average cleanup time {}ms", sleepTime);
      while (true) {
        Thread.sleep(sleepTime);
        log.trace("Starting cleanup");
        Set<K> keys = map.keySet();
        int i = 0;
        for (K key : keys) {
          ICacheable<K, V> entry = map.get(key);
          if (entry.isExpired()) {
//            log.trace("Record created at -> {}", entry.getCreatedTime());
//            log.trace("Record expired at -> {}", entry.getExpireTime());
            remove(entry);
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
      EvictionProtocol evictionProtocol, boolean enableEviction,
      List<RemovalListener<K, V>> removalListeners) {
    log.trace("Cache()");
    this.ttl = ttl;
    this.refreshTtl = refreshTtlOnAccess;
    this.removalListeners.addAll(removalListeners);
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
    Thread eviction = new Thread(() -> this.evictionProtocol.evict(this.evictionSleepTime));
    eviction.setPriority(Thread.MIN_PRIORITY);
    eviction.setDaemon(true);
    log.trace("Starting cleanup thread");
    if (enableEviction) {
      eviction.start();
    }
  }

  public static <K extends Serializable, V> Cache.CacheBuilder<K, V> builder() {
    return Cache.CacheBuilder.builder();
  }


  private void remove(ICacheable<K, V> record) {
    if (record == null) {
      log.trace("No record was found to be removed");
      return;
    }
    log.trace("Removing record -> {}", record.getId());
    for (RemovalListener<K, V> removalListener : removalListeners) {
      removalListener.onRemoval(new RemovalEvent<>(record.getId(), record.getValue()));
    }
    map.remove(record.getId());
  }

  public void invalidate(K key) {
    remove(map.get(key));
  }

  public K put(CachedRecord<K, V> entry) {
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
          remove(entry);
          return null;
        }
        V value = entry.getValue();
        log.trace("Returning entry from cache -> {}:{}", key, value);
        return value;
      }
      return null;
    }
  }

  public static class CacheBuilder<K extends Serializable, V> {

    private Long ttl;
    private Long evictionSleepTime;
    private boolean refreshTtlOnAccess;
    private EvictionProtocol evictionProtocol;
    private boolean implicitEvicition;
    private boolean enableEviction;
    private List<RemovalListener<K, V>> removalListeners = new ArrayList<>();

    CacheBuilder() {
    }

    static <K extends Serializable, V> CacheBuilder<K, V> builder() {
      return new CacheBuilder<>();
    }

    public CacheBuilder<K, V> ttl(long ttl) {
      this.ttl = ttl;
      this.implicitEvicition = true;
      return this;
    }

    public CacheBuilder<K, V> refreshTtlOnAccess(boolean refreshTtlOnAccess) {
      this.refreshTtlOnAccess = refreshTtlOnAccess;
      this.implicitEvicition = true;
      return this;
    }

    public CacheBuilder<K, V> evictionProtocol(EvictionProtocol evictionProtocol) {
      this.evictionProtocol = evictionProtocol;
      this.implicitEvicition = true;
      return this;
    }

    public CacheBuilder<K, V> evictionSleepTime(long evictionSleepTime) {
      this.evictionSleepTime = evictionSleepTime;
      this.implicitEvicition = true;
      return this;
    }

    public CacheBuilder<K, V> enableEviction(boolean enableEviction) {
      this.enableEviction = enableEviction;
      return this;
    }

    public CacheBuilder<K, V> removalListener(RemovalListener<K, V> removalListener) {
      this.removalListeners.add(removalListener);
      return this;
    }

    public Cache<K, V> build() {
      return new Cache<K, V>(ttl, refreshTtlOnAccess, evictionSleepTime, evictionProtocol,
          implicitEvicition || enableEviction, removalListeners) {
      };
    }
  }

}
