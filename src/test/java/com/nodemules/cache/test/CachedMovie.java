package com.nodemules.cache.test;

import com.nodemules.cache.core.CachedRecord;
import com.nodemules.cache.core.ICacheable;
import java.util.UUID;

/**
 * @author brent
 * @since 7/28/18.
 */
public class CachedMovie extends CachedRecord<UUID, Movie> implements ICacheable<UUID, Movie> {

  private static final long serialVersionUID = 2483476462532194837L;

  public CachedMovie(Movie movie) {
    this(movie, null);
  }

  public CachedMovie(Movie movie, Long ttl) {
    this(movie, ttl, false);
  }

  public CachedMovie(Movie movie, Long ttl, boolean refreshTtlOnAccess) {
    super(UUID.randomUUID(), movie, ttl, refreshTtlOnAccess);
  }

  @Override
  public Movie getValue() {
    return super.getValue(Movie.class);
  }
}
