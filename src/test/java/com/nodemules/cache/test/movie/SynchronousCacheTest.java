package com.nodemules.cache.test.movie;

import com.nodemules.cache.core.Cache;
import com.nodemules.cache.core.CachedRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author brent
 * @since 7/28/18.
 */
@Slf4j
@ActiveProfiles("stress")
public class SynchronousCacheTest extends MovieCacheTest {

  private Cache<UUID, Movie> cache;

  public SynchronousCacheTest() {
    log.debug("CacheTest()");
  }

  @Before
  public void before() {
    cache = MovieCache.builder()
        .evictionSleepTime(100)
        .build();
  }

  @Test
  public void testGet_withSynchronousAccess() throws InterruptedException {
    final int tests = 10;

    final int count = 50_000;
    final int threads = 20;
    final int expected = count * threads;

    final float timePerRecordInNano = 200;
    final float timePerBatch = count * (timePerRecordInNano / 1000) / 2;

    final float avgMissPercentage = 0.0001f;
    final float maxMissPercentage = avgMissPercentage * 5;
    final int maxMissThreshold = (int) (expected * (1f - maxMissPercentage));
    final int avgMissThreshold = (int) (expected * (1f - avgMissPercentage));
    final int acceptableMaxMisses = expected - maxMissThreshold;
    final int acceptableAvgMisses = expected - avgMissThreshold;

    List<Integer> missCount = new ArrayList<>();
    List<Integer> timesPerBatch = new ArrayList<>();

    for (int t = 0; t < tests; t++) {
      List<Movie> retrievedMovies = new ArrayList<>();
      Movie movie = generateMovie();
      CachedRecord<UUID, Movie> entry = new CachedMovie(movie);
      UUID id = cache.put(entry);
      ExecutorService executorService = Executors.newFixedThreadPool(threads);
      for (int i = 0; i < threads; i++) {
        executorService.submit(() -> {
          long start = System.currentTimeMillis();
          for (int j = 0; j < count; j++) {
            Movie retrieved = cache.get(id);
            retrievedMovies.add(retrieved);
          }
          long end = System.currentTimeMillis();
          long duration = end - start;
          log.debug("Retrieving {} records took {}ms", count, duration);
          timesPerBatch.add((int) duration);
          assert !(duration > timePerBatch);
        });
      }
      executorService.shutdown();
      executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      final int retrievedSize = retrievedMovies.size();
      final int misses = expected - retrievedSize;
      log.debug("Expected -> {}, Retrieved -> {}, Misses -> {}, Acceptable -> MAX:{}, AVG:{}",
          expected, retrievedSize, misses, acceptableMaxMisses, acceptableAvgMisses);
      log.debug("Cache misses -> {}", misses);
      missCount.add(misses);
    }
    final int mostMisses = missCount.stream().mapToInt(Integer::intValue).max().orElse(0);
    final double avgMisses = missCount.stream().mapToInt(Integer::intValue).average().orElse(0);
    final double maxTimePerBatch = timesPerBatch.stream().mapToInt(Integer::intValue).max()
        .orElse(0);
    final double avgTimePerBatch = timesPerBatch.stream().mapToInt(Integer::intValue).average()
        .orElse(0);
    final double acceptableAvgTimePerBatch = (timePerBatch / 2);
    log.info("=======TEST RESULTS testGet_withSynchronousAccess() TEST RESULTS=======");
    log.info("Expected -> {}", expected);
    log.info("Maximum misses -> {}", mostMisses);
    log.info("Average misses -> {}", avgMisses);
    log.info("Acceptable misses -> {}", acceptableMaxMisses);
    log.info("Expected retrieval time per record -> {}ns", timePerRecordInNano);
    log.info("Expected retrieval time per batch -> {}ms", timePerBatch);
    log.info("Expected average retrieval time per batch -> {}ms", acceptableAvgTimePerBatch);
    log.info("Max time per batch -> {}", maxTimePerBatch);
    log.info("Average time per batch -> {}", avgTimePerBatch);
    log.info("=======TEST RESULTS testGet_withSynchronousAccess() TEST RESULTS=======");
    assert mostMisses < acceptableMaxMisses;
    assert avgMisses < acceptableAvgMisses;
    assert avgTimePerBatch < acceptableAvgTimePerBatch;
  }
}
