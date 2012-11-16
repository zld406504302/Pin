package pin.redis.jedis.operation;

import redis.clients.jedis.Jedis;

public interface Execute<T> {
	/**
	 * 定义reids执行操作
	 * 
	 * @param jedis
	 *            {@link Jedis}
	 * @return 返回执行结果
	 */
	T execute(Jedis jedis);
}
