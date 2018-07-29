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
public class CacheTest extends AbstractTestRunner {

  private static final Cache<UUID, Movie> cache = MovieCache.builder()
      .evictionSleepTime(100)
      .build();

  public CacheTest() {
    log.debug("CacheTest()");
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
  public void testGet() {
    log.debug("testGet()");
    Movie movie = generateMovie();
    CachedRecord<UUID, Movie> entry = new CachedMovie(movie);
    UUID id = cache.put(entry);
    sleep(100);
    Movie retrieved = cache.get(id);

    assert movie.getName().equals(retrieved.getName());
    log.debug("testGet() success");
  }

  @Test
  public void testGet_andExpired() {
    log.debug("testGet_andExpired()");
    Movie movie = generateMovie();
    CachedRecord<UUID, Movie> entry = new CachedMovie(movie, 100L);
    UUID id = cache.put(entry);
    sleep(200);
    Movie retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved == null;
    log.debug("testGet_andExpired() success");
  }

  @Test
  public void testGet_andRefresh() {
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
