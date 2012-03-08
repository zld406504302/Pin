package com.liteProto;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;



public class MessageHandler extends SimpleChannelHandler {

	private LlpProtocolManager protocolManager;

	public MessageHandler(LlpProtocolManager protocolManager) {
		this.protocolManager = protocolManager;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		LlpMessage msg = (LlpMessage) e.getMessage();
		protocolManager.handleMessage(ctx, msg);
	}
}
