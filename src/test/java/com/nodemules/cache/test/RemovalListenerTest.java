package com.nodemules.cache.test;

import com.nodemules.cache.core.Cache;
import com.nodemules.cache.test.movie.CachedMovie;
import com.nodemules.cache.test.movie.Movie;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

/**
 * @author brent
 * @since 7/29/18.
 */
@Slf4j
public class RemovalListenerTest extends MovieCacheTest {

  private int removals;
  private Cache<UUID, Movie> cache;

  @Before
  public void before() {
    removals = 0;
    cache = Cache.<UUID, Movie>builder()
        .removalListener(event -> {
          log.info("{}:{} was removed from the cache", event.getId(), event.getValue().getName());
        })
        .removalListener(event -> {
          removals++;
        })
//        .ttl(100)
        .evictionSleepTime(100)
        .build();
  }

  @Test
  public void testRemovalCounter() {
    int count = 100;
    for (int i = 0; i < count; i++) {
      cache.put(new CachedMovie(generateMovie(),
          (long) Math.max(50, Math.min(200, Math.random() * 1000))));
    }

    sleep(1000);
    log.info("removals -> {}", removals);
    assert removals == count;
  }

  @Test
  public void testRemovalListener_withManualRemoval() {
    UUID id = cache.put(new CachedMovie(generateMovie()));
    cache.invalidate(id);

    assert removals == 1;
  }
}
