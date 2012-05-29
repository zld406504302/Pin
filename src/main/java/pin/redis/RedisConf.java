package pin.redis;

import redis.clients.jedis.JedisPoolConfig;

/**
 * redis 配置静态类
 * 
 * @author zhongyuan
 * 
 */
public final class RedisConf {
	public static final int MAX_ACTIVE = 100;
	public static final int MAX_IDLE = 20;
	public static final int MAX_WAIT = 1000;
	public static final boolean TEST_ON_BORROW = true;

	/**
	 * 私有构造函数
	 */
	private RedisConf() {

	}

	/**
	 * 获得默认的redis配置
	 * 
	 * @return 默认reids配置
	 */
	public static JedisPoolConfig getDefaultConf() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(RedisConf.MAX_ACTIVE);
		config.setMaxIdle(RedisConf.MAX_IDLE);
		config.setMaxWait(RedisConf.MAX_WAIT);
		config.setTestOnBorrow(RedisConf.TEST_ON_BORROW);

		return config;
	}
}
