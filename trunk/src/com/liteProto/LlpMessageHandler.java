package com.liteProto;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;



public class LlpMessageHandler extends SimpleChannelHandler {

	private LlpProtocolManager protocolManager;

	public LlpMessageHandler(LlpProtocolManager protocolManager) {
		this.protocolManager = protocolManager;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		LlpMessage msg = null;
		try {
			msg = (LlpMessage) e.getMessage();
			protocolManager.handleMessage(ctx, msg);
		} finally {
			if(msg != null) {
				msg.destory();
			}
		}
	}
}
