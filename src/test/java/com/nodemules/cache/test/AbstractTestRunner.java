package com.nodemules.cache.test;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author brent
 * @since 7/29/18.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CacheTest.class, CacheTtlTest.class})
@ActiveProfiles("test")
public abstract class AbstractTestRunner {

}
