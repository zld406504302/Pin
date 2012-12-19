package pin.net.http;

/**
 * Created with IntelliJ IDEA.
 * User: lizhongyuan
 * Date: 12-12-18
 * Time: 下午4:16
 */
public interface RequestHandler<VO> {

    /**
     * 处理网络请求
     * @return 要返回的JSON数据
     */
    String handleRequest(VO data);
}
