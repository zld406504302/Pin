package pin.http;

/**
 * Created with IntelliJ IDEA.
 * User: zhongyuan
 * Date: 12-9-23
 * Time: 上午1:25
 */
public class HttpResponseInfo {
    private boolean isResponseSuccess;
    private byte[] data;

    public boolean isResponseSuccess() {
        return isResponseSuccess;
    }

    public void setResponseSuccess(boolean responseSuccess) {
        isResponseSuccess = responseSuccess;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
