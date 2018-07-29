package com.nodemules.cache.test;

import com.github.javafaker.Faker;
import com.nodemules.cache.test.movie.Movie;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author brent
 * @since 7/29/18.
 */
public abstract class MovieCacheTest extends AbstractTestRunner {

  private static final Faker faker = Faker.instance();

  protected static Movie generateMovie() {
    Movie movie = new Movie();
    movie.setMovieId(UUID.randomUUID());
    movie.setName(String.format("The %s %s of %s", faker.demographic().maritalStatus(),
        faker.demographic().demonym(), faker.educator().campus().split(" ")[0]));
    Date date = faker.date().past(365 * 100, TimeUnit.DAYS);
    movie.setYear(
        String.valueOf(
            LocalDate.from(date.toInstant().atZone(ZoneId.systemDefault())).getYear()));
    movie.setDirectors(Collections.singletonList(faker.name().fullName()));
    return movie;
  }
}
