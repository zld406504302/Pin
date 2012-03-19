package com.liteProto;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import pin.net.protocol.ProtocolHandler;



public class LlpChannelHandler extends SimpleChannelHandler {

	protected Map<String, ProtocolHandler> handlerMap = new HashMap<String, ProtocolHandler>();

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		LlpMessage msg = (LlpMessage) e.getMessage();
		try {
			ProtocolHandler handler = handlerMap.get(msg.getName());
			if (handler == null) {
				Channels.fireExceptionCaught(ctx.getChannel(), new RuntimeException("can not find handler for " + msg.getName()));
			}
			handler.preHandle(ctx);
			handler.handleReceived(ctx, msg);
			handler.handleReply(ctx);
		} finally {
			msg.destory();
		}
	}

	public void regProtocolHandler(String key, ProtocolHandler handler) {
		handlerMap.put(key, handler);
	}

}
