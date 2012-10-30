package pin.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: zhongyuan
 * Date: 12-9-21
 * Time: 下午6:06
 */
public class HttpUtil {
    private static HttpClient client = new DefaultHttpClient();

    public static HttpResponseInfo httpGet(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse response = client.execute(get);
            HttpResponseInfo info = new HttpResponseInfo();
            if (isStatusSuccess(response.getStatusLine().getStatusCode())) {
                byte[] data = new byte[(int) response.getEntity().getContentLength()];
                InputStream inputStream = response.getEntity().getContent();
                int readPos = 0;
                int readDataNum = 0;
                while ((readDataNum = inputStream.read(data, readPos, data.length - readPos)) > 0) {
                    readPos += readDataNum;
                }
                info.setResponseSuccess(true);
                info.setData(data);
            }

            return info;
        } finally {
            get.releaseConnection();
        }
    }

    private static boolean isStatusSuccess(int status) {
        return (status >= 200 && status < 300);
    }


}
