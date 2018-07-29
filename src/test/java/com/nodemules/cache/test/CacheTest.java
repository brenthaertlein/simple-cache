package com.nodemules.cache.test;

import com.nodemules.cache.core.Cache;
import com.nodemules.cache.core.ICacheable;
import java.util.Collections;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author brent
 * @since 7/28/18.
 */
@Slf4j
public class CacheTest {

  public CacheTest() {
    log.debug("CacheTest()");
  }

  private static final Cache<UUID, Movie> cache = new MovieCache();

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
    ICacheable<UUID, Movie> entry = new CachedMovie(generateMovie());
    UUID id = cache.put(entry);

    assert (id != null);
    log.debug("testPut() success");
  }

  @Test
  public void testGet() {
    log.debug("testGet()");
    Movie movie = generateMovie();
    ICacheable<UUID, Movie> entry = new CachedMovie(movie);
    UUID id = cache.put(entry);
    sleep(5000);
    Movie retrieved = cache.get(id);

    assert movie.getName().equals(retrieved.getName());
    log.debug("testGet() success");
  }

  @Test
  public void testGet_andExpired() {
    log.debug("testGet_andExpired()");
    Movie movie = generateMovie();
    ICacheable<UUID, Movie> entry = new CachedMovie(movie, 1000L);
    UUID id = cache.put(entry);
    sleep(6000);
    Movie retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved == null;
    log.debug("testGet_andExpired() success");
  }

  @Test
  public void testGet_andRefresh() {
    log.debug("testGet_andExpired()");
    Movie movie = generateMovie();
    ICacheable<UUID, Movie> entry = new CachedMovie(movie, 3000L, true);
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

  static void sleep(long time) {
    try {
      log.debug("Test sleeping for {}ms", time);
      Thread.sleep(time);
    } catch (InterruptedException e) {
    }
    log.debug("Test done sleeping");
  }
}
