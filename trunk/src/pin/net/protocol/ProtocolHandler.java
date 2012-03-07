package pin.net.protocol;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.liteProto.LlpMessage;

public interface ProtocolHandler {
	public void handleReceivedMessage(LlpMessage msg);
	public void handleReply(ChannelHandlerContext ctx);
}
