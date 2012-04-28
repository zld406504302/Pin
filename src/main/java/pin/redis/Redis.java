package pin.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pin.redis.operation.Execute;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis {

	private static Redis instance = new Redis();
	private JedisPool pool = null;
	private Logger logger = LoggerFactory.getLogger(Redis.class);

	private Redis() {

	}

	public static Redis instance() {
		return instance;
	}

	public void init(JedisPoolConfig config, String host, int port) {
		pool = new JedisPool(config, host, port);
	}

	public void init(JedisPoolConfig config, String host, int port, String passwd) {
		pool = new JedisPool(config, host, port, 2000, passwd);
	}

	public <T> T execute(Execute<T> op) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			T result = op.execute(jedis);
			pool.returnResource(jedis); // 不使用finally方式
			return result;
		} catch (Exception e) {
			if (pool != null) {
				pool.returnBrokenResource(jedis);
			}
			logger.error("error on execute redis operation: ", e);
		}

		return null;
	}

	public Jedis getResource() {
		return pool.getResource();
	}

	public void returnResource(Jedis jedis) {
		pool.returnResource(jedis);
	}

	public void returnBrokenResource(Jedis jedis) {
		pool.returnBrokenResource(jedis);
	}
}
