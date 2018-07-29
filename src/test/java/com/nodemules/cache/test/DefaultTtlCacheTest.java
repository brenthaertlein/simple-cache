package com.nodemules.cache.test;

import com.nodemules.cache.core.Cache;
import com.nodemules.cache.core.CachedRecord;
import java.util.Collections;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author brent
 * @since 7/28/18.
 */
@Slf4j
public class DefaultTtlCacheTest extends AbstractTestRunner {

  private static final Cache<UUID, Movie> cache = MovieCache.builder()
      .evictionSleepTime(100)
      .ttl(200)
      .refreshTtlOnAccess(true)
      .build();

  public DefaultTtlCacheTest() {
    log.debug("DefaultTtlCacheTest()");
  }

  private static Movie generateMovie() {
    Movie movie = new Movie();
    movie.setMovieId(UUID.randomUUID());
    movie.setName("Jurassic Park");
    movie.setYear("1993");
    movie.setDirectors(Collections.singletonList("Stephen Spielberg"));
    return movie;
  }

  @Test
  public void testPut() {
    log.debug("testPut()");
    CachedRecord<UUID, Movie> entry = new CachedMovie(generateMovie());
    UUID id = cache.put(entry);

    assert (id != null);
    log.debug("testPut() success");
  }

  @Test
  public void testGet_andExpired_withDefaultCacheTtl() {
    log.debug("testGet_andExpired()");
    CachedRecord<UUID, Movie> entry = new CachedMovie(generateMovie());
    UUID id = cache.put(entry);
    sleep(300);
    Movie retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved == null;
    log.debug("testGet_andExpired() success");
  }

  @Test
  public void testGet_andExpired_withEntryTtl() {
    log.debug("testGet_andExpired()");
    CachedRecord<UUID, Movie> entry = new CachedMovie(generateMovie());
    UUID id = cache.put(entry);
    sleep(300);
    Movie retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved == null;
    log.debug("testGet_andExpired() success");
  }

  @Test
  public void testGet_andRefresh_withEntryTtl() {
    log.debug("testGet_andExpired()");
    Movie movie = generateMovie();
    CachedRecord<UUID, Movie> entry = new CachedMovie(movie, 300L, true);
    UUID id = cache.put(entry);
    sleep(200);
    Movie retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved != null;
    sleep(200);

    retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved != null;

    sleep(500);

    retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved == null;

    log.debug("testGet_andExpired() success");
  }
}
