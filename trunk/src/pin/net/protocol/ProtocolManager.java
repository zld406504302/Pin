package pin.net.protocol;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;

import com.liteProto.LlpMessage;

public abstract class ProtocolManager {
	protected Map<String, ProtocolHandler> handlerMap = new HashMap<String, ProtocolHandler>();
	
	public ProtocolManager() {
		regProtocolHandler();
	}
	
	protected abstract void regProtocolHandler();
	
	public void handleMessage(ChannelHandlerContext ctx, LlpMessage message) {
		ProtocolHandler handler = handlerMap.get(message.getName());
		if(handler == null)
			Channels.fireExceptionCaught(ctx.getChannel(), new RuntimeException("can not find handler for " + message.getName()));
		
		handler.handleReceivedMessage(message);
		message.destory();
		handler.handleReply(ctx);
	}
}
