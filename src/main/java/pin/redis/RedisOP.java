package pin.redis;

import redis.clients.jedis.Jedis;

public interface RedisOP<T> {
	T execute(Jedis jedis);
}
