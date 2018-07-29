package com.nodemules.cache.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

/**
 * @author brent
 * @since 7/28/18.
 */
@Data
public class Movie {

  private UUID movieId;
  private String name;
  private String year;
  private List<String> directors = new ArrayList<>();
}
