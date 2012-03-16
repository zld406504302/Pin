package redis.clients;

import org.junit.Assert;
import org.junit.Test;

import redis.clients.jedis.Jedis;

public class TestJedis {

	@Test
	public void test() {
		Jedis jedis = new Jedis("192.168.0.246");
		jedis.set("foo", "bar");
		Assert.assertEquals("bar", jedis.get("foo"));
	}

}
