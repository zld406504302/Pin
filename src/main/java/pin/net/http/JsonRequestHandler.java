package pin.net.http;

import com.alibaba.fastjson.JSON;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;
import org.springframework.core.GenericTypeResolver;
import pin.spring.Spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created with IntelliJ IDEA.
 * User: zhongyuan
 * Date: 12-9-11
 * Time: 上午11:33
 */
public class JsonRequestHandler extends SimpleChannelUpstreamHandler {
    private Map<String, RequestHandler> handlersMap = new HashMap<>();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();

        boolean keepAlive = isKeepAlive(request);

        //解析HTTP请求
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> params = queryStringDecoder.getParameters();
        String reqJson = params.get("reqJson").get(0);
        if (reqJson != null) {
            String pathWithSlash = queryStringDecoder.getPath();
            String path = pathWithSlash.substring(1, pathWithSlash.length());

            //获取requestHandler
            RequestHandler requestHandler;
            if (handlersMap.containsKey(path)) {
                requestHandler = handlersMap.get(path);
            } else {
                requestHandler = Spring.instance().getBean(path, RequestHandler.class);
                handlersMap.put(path, requestHandler);
            }

            Class<?> dataClass = GenericTypeResolver.resolveTypeArgument(requestHandler.getClass(), RequestHandler.class);
            Object reqData = JSON.parseObject(reqJson, dataClass);
            //处理request
            @SuppressWarnings("unchecked")
            String jsonResponse = requestHandler.handleRequest(reqData);

            //处理回复
            writeResponse(jsonResponse, e.getChannel(), keepAlive);
        } else {
            writeResponse("出错啦!", e.getChannel(), keepAlive);
        }
    }

    private void writeResponse(String content, Channel channel, boolean isKeepAlive) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        if (isKeepAlive) {
            response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
            response.setHeader(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // Write the response.
        ChannelFuture future = channel.write(response);

        if (!isKeepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
