package pin.hash;

/**
 * Created with IntelliJ IDEA.
 * User: lizhongyuan
 * Date: 12-11-5
 * Time: 下午6:35
 */
public class BKDRHash implements StringHashFunction {

    @Override
    public long hash(String str) {
        long seed = 131; // 31 131 1313 13131 131313 etc..
        long hash = 0;

        for(int i = 0; i < str.length(); i++)
        {
            hash = (hash * seed) + str.charAt(i);
        }

        return hash;
    }
}
