package pin.hash;

/**
 * Created with IntelliJ IDEA.
 * User: lizhongyuan
 * Date: 12-11-5
 * Time: 下午6:39
 */
public class JSHash implements StringHashFunction {

    @Override
    public long hash(String str) {
        long hash = 1315423911;

        for (int i = 0; i < str.length(); i++) {
            hash ^= ((hash << 5) + str.charAt(i) + (hash >> 2));
        }

        return hash;
    }
}
