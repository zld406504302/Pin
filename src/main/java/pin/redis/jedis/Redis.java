package pin.redis.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pin.redis.jedis.operation.Execute;
import pin.spring.Spring;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public final class Redis {

	private static Redis instance = new Redis();
	private JedisPool pool = Spring.instance().getBean("jedisPool", JedisPool.class);
	private static Logger logger = LoggerFactory.getLogger(Redis.class);

	/**
	 * 私有构造函数
	 */
	private Redis() {

	}

	/**
	 * 获取redis实例
	 * 
	 * @return redis实例
	 */
	public static Redis instance() {
		return instance;
	}

	/**
	 * 执行redis操作
	 * 
	 * @param op
	 *            redis操作
	 * @param <T>
	 *            返回的类型
	 * @return 返回值
	 */
	public static <T> T execute(Execute<T> op) {
		Jedis jedis = null;
		try {
			jedis = instance().pool.getResource();
			T result = op.execute(jedis);
			instance().pool.returnResource(jedis); // 不使用finally方式
			return result;
		} catch (Exception e) {
			if (instance().pool != null) {
				instance().pool.returnBrokenResource(jedis);
			}
			logger.error("error on execute redis operation: ", e);
		}

		return null;
	}

	/**
	 * 异步执行redis操作
	 * 
	 * @param op
	 *            redis操作
	 * @param <T>
	 *            返回的类型
	 */
	public <T> void asyncExecute(Execute<T> op) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 获得redis连接
	 * 
	 * @return {@link Jedis}
	 */
	public Jedis getResource() {
		return pool.getResource();
	}

	/**
	 * 归还redis连接资源
	 * 
	 * @param jedis
	 *            {@link Jedis}
	 */
	public void returnResource(Jedis jedis) {
		pool.returnResource(jedis);
	}

	/**
	 * 归还出错的reids连接资源
	 * 
	 * @param jedis
	 *            {@link Jedis}
	 */
	public void returnBrokenResource(Jedis jedis) {
		pool.returnBrokenResource(jedis);
	}
}
