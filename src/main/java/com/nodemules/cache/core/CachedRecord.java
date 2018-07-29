package com.nodemules.cache.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
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

  @Getter
  private K id;

  private boolean refreshTtl;

  private Long created;
  private Long expires;
  private Long accessed;

  @Getter(AccessLevel.PACKAGE)
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
    this.created = ZonedDateTime.now().toInstant().toEpochMilli();
    try {
      data = new ObjectMapper().writeValueAsString(value);
    } catch (JsonProcessingException e) {
    }
    if (ttl != null) {
      this.ttl = ttl;
      this.expires = created + ttl;
      this.refreshTtl = refreshTtlOnAccess;
      log.trace("{} created -> {}, expires -> {}", id, getCreatedTime(), getExpireTime());
    }
  }

  @Override
  public ZonedDateTime getCreatedTime() {
    log.trace("{} created -> {}", id, expires);
    if (expires == null) {
      return null;
    }
    return ZonedDateTime.from(Instant.ofEpochMilli(created).atZone(ZoneId.systemDefault()));
  }

  @Override
  public ZonedDateTime getExpireTime() {
    log.trace("{} expires -> {}", id, expires);
    if (expires == null) {
      return null;
    }
    return ZonedDateTime.from(Instant.ofEpochMilli(expires).atZone(ZoneId.systemDefault()));
  }

  void setExpireTime(long ttl) {
    this.ttl = ttl;
    log.trace("Setting ttl -> {}", ttl);
    this.expires = calculateEpochTimeFromTtl(ZonedDateTime.now(), ttl);
    log.trace("Setting expires -> {}", expires);
  }

  void setExpireTime(long ttl, boolean refreshTtlOnAccess) {
    setExpireTime(ttl);
    this.refreshTtl = refreshTtlOnAccess;
  }

  @Override
  public boolean isExpired() {
    if (expires != null) {
      return Date.from(Instant.now()).after(Date.from(Instant.ofEpochMilli(expires)));
    }
    return false;
  }

  void access() {
    if (refreshTtl && expires != null) {
      log.trace("Refreshing access for {}ms -> {}", ttl, id);
      this.accessed = ZonedDateTime.now().toInstant().toEpochMilli();
      expires = accessed + ttl;
    }
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


  private long calculateEpochTimeFromTtl(ZonedDateTime time, long ttl) {
    return time.plus(ttl, ChronoField.MILLI_OF_DAY.getBaseUnit()).toInstant()
        .toEpochMilli();
  }

}
