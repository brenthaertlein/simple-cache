package com.nodemules.cache.test;

import com.nodemules.cache.test.movie.CacheTest;
import com.nodemules.cache.test.movie.DefaultTtlCacheTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author brent
 * @since 7/29/18.
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CacheTest.class, DefaultTtlCacheTest.class})
@ActiveProfiles("test")
public abstract class AbstractTestRunner {


  protected static void sleep(long time) {
    try {
      log.debug("Test sleeping for {}ms", time);
      Thread.sleep(time);
    } catch (InterruptedException e) {
    }
    log.debug("Test done sleeping");
  }
}
