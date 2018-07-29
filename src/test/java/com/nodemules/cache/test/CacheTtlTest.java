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
public class CacheTtlTest extends AbstractTestRunner {

  private static final Cache<UUID, Movie> cache = new MovieCache(2000L);

  public CacheTtlTest() {
    log.debug("CacheTtlTest()");
  }

  private static Movie generateMovie() {
    Movie movie = new Movie();
    movie.setMovieId(UUID.randomUUID());
    movie.setName("Jurassic Park");
    movie.setYear("1993");
    movie.setDirectors(Collections.singletonList("Stephen Spielberg"));
    return movie;
  }

  static void sleep(long time) {
    try {
      log.debug("Test sleeping for {}ms", time);
      Thread.sleep(time);
    } catch (InterruptedException e) {
    }
    log.debug("Test done sleeping");
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
    Movie movie = generateMovie();
    CachedRecord<UUID, Movie> entry = new CachedMovie(movie);
    UUID id = cache.put(entry);
    sleep(3000);
    Movie retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved == null;
    log.debug("testGet_andExpired() success");
  }

  @Test
  public void testGet_andExpired_withEntryTtl() {
    log.debug("testGet_andExpired()");
    Movie movie = generateMovie();
    CachedRecord<UUID, Movie> entry = new CachedMovie(movie, 1000L);
    UUID id = cache.put(entry);
    sleep(2000);
    Movie retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved == null;
    log.debug("testGet_andExpired() success");
  }

  @Test
  public void testGet_andRefresh_withEntryTtl() {
    log.debug("testGet_andExpired()");
    Movie movie = generateMovie();
    CachedRecord<UUID, Movie> entry = new CachedMovie(movie, 3000L, true);
    UUID id = cache.put(entry);
    sleep(2000);
    Movie retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved != null;
    sleep(2000);

    retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved != null;

    sleep(5000);

    retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved == null;

    log.debug("testGet_andExpired() success");
  }
}