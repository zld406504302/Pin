package com.pin.redis.spring;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import pin.redis.spring.SpringRedisPreshardingCluster;
import pin.spring.Spring;

/**
 * Created with IntelliJ IDEA. User: lizhongyuan Date: 12-11-14 Time: 下午5:00
 */
public class TestSpringRedisPresharding {

	@Before
	public void setUp() throws Exception {
		Spring.instance().init("springRedisCluster.xml");
	}

    @Ignore
	@Test
	public void testSpringRedisPresharding() throws Exception {
		String testKey1 = "aaaaaa";
		String testKey2 = "aaabbb";
		String testKey3 = "aaa[bbb";
		String testKey4 = "aaa]bbb";
		String testKey5 = "aaa[bbb]";
		String testKey6 = "[bbb]aaa";
		String testKey7 = "aaa[bbb]aaa";
		String testKey8 = "user[888547885477]:base";

		SpringRedisPreshardingCluster redisPresharding = Spring.instance().getBean("redisPreshardingCluster", SpringRedisPreshardingCluster.class);
		StringRedisTemplate template = redisPresharding.getRedisTemplate(testKey1);

		template.opsForValue().set("test", "result");

		RedisTemplate template2 = redisPresharding.getRedisTemplate(testKey1);

		String result = (String) template2.opsForValue().get("test");

		Assert.assertEquals("result", result);

		StringRedisTemplate templateKey5 = redisPresharding.getRedisTemplate(testKey5);
		StringRedisTemplate templateKey6 = redisPresharding.getRedisTemplate(testKey6);
		StringRedisTemplate templateKey7 = redisPresharding.getRedisTemplate(testKey7);

		templateKey5.opsForValue().set("testKey5", testKey5);
		String resultKey6 = (String) templateKey6.opsForValue().get("testKey5");
		String resultKey7 = (String) templateKey7.opsForValue().get("testKey5");

		Assert.assertEquals(resultKey6, resultKey7);

		redisPresharding.getRedisTemplate(testKey8);
	}
}
