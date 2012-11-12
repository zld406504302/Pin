package pin.redis.spring;

import org.springframework.data.redis.core.RedisTemplate;
import pin.hash.StringHashFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: lizhongyuan
 * Date: 12-11-6
 * Time: 下午4:11
 */
public class SpringRedisPresharding {
    private List<RedisTemplate> instances;
    private Map<String, List<RedisTemplate>> namedInstances;
    private StringHashFunction hashFunction;


    public RedisTemplate getRedisTemplate(String key) {
        return instances.get((int) (hashFunction.hash(key) % instances.size()));
    }

    public RedisTemplate getRedisTemplate(String key, String instanceName) {
        List<RedisTemplate> instances = namedInstances.get(instanceName);
        if (instances != null) {
            return instances.get((int) (hashFunction.hash(key) % instances.size()));
        } else {
            return null;
        }
    }

    public void setHashFunction(StringHashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public void setInstances(List<RedisTemplate> instances) {
        this.instances = instances;
    }

    public void setNamedInstances(Map<String, List<RedisTemplate>> namedInstances) {
        this.namedInstances = namedInstances;
    }
}
