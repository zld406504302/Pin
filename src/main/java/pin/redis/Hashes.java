package pin.redis;

import java.util.Map;

public interface Hashes {

	/**
	 * 返回将要写入redis中的key value数据
	 * 
	 * @return 将要写入redis中的key value数据
	 */
	Map<String, String> hashEncoder();

	/**
	 * 解析redis hashes中的数据
	 * 
	 * @param hashes
	 *            redis中的hash数据
	 */
	void hashDecoder(Map<String, String> hashes);
}
