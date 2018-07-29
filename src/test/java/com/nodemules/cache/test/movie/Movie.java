package com.nodemules.cache.test.movie;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author brent
 * @since 7/28/18.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Movie {

  private UUID movieId;
  private String name;
  private String year;

  @Builder.Default
  private List<String> directors = new ArrayList<>();
}
