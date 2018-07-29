package com.nodemules.cache.test.movie;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.ToString;

/**
 * @author brent
 * @since 7/28/18.
 */
@Data
@ToString
public class Movie {

  private UUID movieId;
  private String name;
  private String year;
  private List<String> directors = new ArrayList<>();
}
