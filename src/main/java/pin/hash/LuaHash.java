package pin.hash;

/**
 * Created with IntelliJ IDEA.
 * User: lizhongyuan
 * Date: 12-11-6
 * Time: 下午3:24
 */
public class LuaHash implements StringHashFunction {

    @Override
    public long hash(String str) {
        int len = str.length();
        long hash = len;
        int step = (len >> 5) + 1;

        for (int i = len; i >= step; i -= step) {
            hash = hash ^ ((hash << 5) + (hash >> 2) + str.charAt(i - 1));
        }
        return hash;
    }
}
