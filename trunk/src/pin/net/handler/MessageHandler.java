package pin.net.handler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import pin.net.protocol.ProtocolManager;

import com.liteProto.LlpMessage;


public class MessageHandler extends SimpleChannelHandler {

	private ProtocolManager protocolManager;
	
	public MessageHandler(ProtocolManager protocolManager) {
		this.protocolManager = protocolManager;
	}
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		LlpMessage msg = (LlpMessage)e.getMessage();
		protocolManager.handleMessage(ctx, msg);
	}

}
