package com.nodemules.cache.test.movie;

import com.nodemules.cache.core.Cache;
import com.nodemules.cache.core.CachedRecord;
import com.nodemules.cache.test.AbstractTestRunner;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

/**
 * @author brent
 * @since 7/28/18.
 */
@Slf4j
public class CacheTest extends MovieCacheTest {

  private Cache<UUID, Movie> cache;

  public CacheTest() {
    log.debug("CacheTest()");
  }

  @Before
  public void before() {
    cache = MovieCache.builder()
        .evictionSleepTime(100)
        .build();
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
    AbstractTestRunner.sleep(100);
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
    AbstractTestRunner.sleep(200);
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
    AbstractTestRunner.sleep(200);
    Movie retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved != null;
    AbstractTestRunner.sleep(200);

    retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved != null;

    AbstractTestRunner.sleep(500);

    retrieved = cache.get(id);
    log.info("{}", retrieved);

    assert retrieved == null;

    log.debug("testGet_andExpired() success");
  }
}
