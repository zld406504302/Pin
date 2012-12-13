package pin.redis.spring;

import org.springframework.data.redis.core.StringRedisTemplate;
import pin.hash.StringHashFunction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: lizhongyuan
 * Date: 12-11-6
 * Time: 下午4:11
 */
public class SpringRedisPreshardingCluster {
    private List<StringRedisTemplate> instances;
    private Map<String, List<StringRedisTemplate>> namedInstances;
    private StringHashFunction hashFunction;

    public StringRedisTemplate getRedisTemplate(String key) {
        String hashTag = getHashTag(key);
        return getInstance(instances, hashFunction.hash(hashTag));
    }

    public StringRedisTemplate getRedisTemplate(String key, String hashTag) {
        return getInstance(instances, hashFunction.hash(hashTag));
    }

    public StringRedisTemplate getNamedRedisTemplate(String key, String instanceName) {
        String hashTag = getHashTag(key);
        List<StringRedisTemplate> instances = namedInstances.get(instanceName);
        if (instances != null) {
            return getInstance(instances, hashFunction.hash(hashTag));
        } else {
            return null;
        }
    }

    public StringRedisTemplate getNamedRedisTemplate(String instanceName) {
        List<StringRedisTemplate> instances = namedInstances.get(instanceName);
        if (instances != null) {
            return instances.get(0);
        } else {
            return null;
        }
    }

    public void setHashFunction(StringHashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public void setInstances(List<StringRedisTemplate> instances) {
        this.instances = instances;
    }

    public void setNamedInstances(Map<String, List<StringRedisTemplate>> namedInstances) {
        this.namedInstances = namedInstances;
    }

    public void flushAll() {
        for(StringRedisTemplate template : instances) {
            template.getConnectionFactory().getConnection().flushAll();
        }

        Collection<List<StringRedisTemplate>> allNamedInstances = namedInstances.values();
        for(List<StringRedisTemplate> namedList : allNamedInstances) {
            for(StringRedisTemplate template : namedList) {
                template.getConnectionFactory().getConnection().flushAll();
            }
        }
    }

    private String getHashTag(String key) {
        String regx = "\\[(.*?)\\]";

        Pattern p = Pattern.compile(regx);
        Matcher m = p.matcher(key);
        if (m.find()) {
            return m.group(m.groupCount());
        } else {
            return key;
        }
    }

    private StringRedisTemplate getInstance(List<StringRedisTemplate> instances, long hashValue) {
        int size = instances.size();
        int index = (int)hashValue % size;
        if(index >= 0) {
            return instances.get(index);
        } else {
            return instances.get(size + index);
        }
    }
}
