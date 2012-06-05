package pin.redis.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pin.redis.Redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public abstract class Cas<Result> {

	private static final int DEFAULT_TRY_TIMES = 100;
	private Logger logger = LoggerFactory.getLogger(Cas.class);

	/**
	 * 执行check之前的处理
	 * 
	 * @param jedis
	 *            {@link Jedis}
	 */
	protected void onPreCheck(Jedis jedis) {

	}

	/**
	 * 执行check操作
	 * 
	 * @param jedis
	 *            {@link Jedis}
	 * @return check是否成功
	 */
	public abstract boolean onCheck(Jedis jedis);

	/**
	 * check成功执行set事务
	 * 
	 * @param trans
	 *            redis 事务 {@link Transaction}
	 */
	public abstract void onSet(Transaction trans);

	/**
	 * 执行redis cas操作
	 * 
	 * @param watchedKeys
	 *            执行过程中要监视的key
	 * @return 执行结果
	 */
	public Result execute(String... watchedKeys) {
		return execute(DEFAULT_TRY_TIMES, watchedKeys);
	}

	/**
	 * 成功执行cas之后的操作
	 * 
	 * @return 执行结果
	 */
	protected abstract Result onExexcuted();

	/**
	 * 执行cas失败之后的操作
	 * 
	 * @return 执行结果
	 */
	protected abstract Result onFailed();

	/**
	 * 执行redis cas操作
	 * 
	 * @param maxTryTime
	 *            最大尝试次数
	 * @param watchedKeys
	 *            执行过程中要监视的key
	 * @return 执行结果
	 */
	public Result execute(int maxTryTime, String... watchedKeys) {
		Jedis jedis = null;
		Result result = null;
		try {
			jedis = Redis.instance().getResource();
			for (int i = 0; i < maxTryTime; i++) {

				jedis.watch(watchedKeys);
				onPreCheck(jedis);

				if (onCheck(jedis)) {
					Object ret = null;
					Transaction trans = jedis.multi();
					onSet(trans);
					ret = trans.exec();
					if (ret == null) {
						continue;
					}

					result = onExexcuted();
				} else {
					jedis.unwatch();
					result = onFailed();
					break;
				}

				if (i == maxTryTime - 1) {
					logger.error("reached max try time:" + maxTryTime);
				}
				break;
			}

			Redis.instance().returnResource(jedis);
			return result;
		} catch (Exception e) {
			if (jedis != null) {
				Redis.instance().returnBrokenResource(jedis);
			}
			logger.error("error on cas: ", e);
		}

		return null;
	}

}