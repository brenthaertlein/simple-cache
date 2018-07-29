package com.nodemules.cache.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author brent
 * @since 7/28/18.
 */
@Slf4j
public abstract class CachedRecord<K extends Serializable, V> implements Serializable,
    ICacheable<K, V> {

  private static final long serialVersionUID = -7742858298757254635L;

  private static final ObjectMapper mapper = new ObjectMapper();

  private K id;

  private boolean refreshTtl;

  private Long expires;
  private Long accessed;

  private Long ttl;

  @Getter(AccessLevel.PRIVATE)
  private String data;
  @Setter(AccessLevel.PRIVATE)
  private transient V value;

  public CachedRecord(K id, V value) {
    this(id, value, null, false);
  }

  public CachedRecord(K id, V value, Long ttl) {
    this(id, value, ttl, false);
  }

  public CachedRecord(K id, V value, Long ttl, boolean refreshTtlOnAccess) {
    this.id = id;
    try {
      data = new ObjectMapper().writeValueAsString(value);
    } catch (JsonProcessingException e) {
    }
    if (ttl != null) {
      this.ttl = ttl;
      this.expires = ZonedDateTime.now().plusNanos(ttl * 1000).toInstant().toEpochMilli();
      this.refreshTtl = refreshTtlOnAccess;
    }
  }

  public void access() {
    if (refreshTtl && expires != null) {
      log.debug("Refreshing access for {}ms -> {}", ttl, id);
      this.accessed = ZonedDateTime.now().toInstant().toEpochMilli();
      expires = accessed + ttl;
    }
  }

  public boolean isExpired() {
    if (expires != null) {
      return Date.from(Instant.now()).after(Date.from(Instant.ofEpochMilli(expires)));
    }
    return false;
  }

  public K getId() {
    return id;
  }

  protected V getValue(Class<V> clazz) {
    if (isExpired()) {
      return null;
    }
    try {
      return mapper.readValue(data, clazz);
    } catch (IOException e) {
      return null;
    }
  }


}
