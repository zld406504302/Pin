package pin.net.protocol;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.liteProto.LlpMessage;

public interface ProtocolHandler {
	public void handleReceivedMessage(ChannelHandlerContext ctx, LlpMessage msg);
	public void handleReply(ChannelHandlerContext ctx) throws Exception;
}
