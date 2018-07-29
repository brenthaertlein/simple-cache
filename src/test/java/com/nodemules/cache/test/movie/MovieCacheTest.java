package com.nodemules.cache.test.movie;

import com.nodemules.cache.test.AbstractTestRunner;
import java.util.Collections;
import java.util.UUID;

/**
 * @author brent
 * @since 7/29/18.
 */
public abstract class MovieCacheTest extends AbstractTestRunner {

  protected static Movie generateMovie() {
    Movie movie = new Movie();
    movie.setMovieId(UUID.randomUUID());
    movie.setName("Jurassic Park");
    movie.setYear("1993");
    movie.setDirectors(Collections.singletonList("Stephen Spielberg"));
    return movie;
  }
}
