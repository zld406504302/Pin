package com.liteProto;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;

import pin.net.protocol.ProtocolHandler;


public abstract class LlpProtocolManager {
	protected Map<String, ProtocolHandler> handlerMap = new HashMap<String, ProtocolHandler>();
	
	public LlpProtocolManager() {
		regProtocolHandler();
	}
	
	protected abstract void regProtocolHandler();
	
	public void handleMessage(ChannelHandlerContext ctx, LlpMessage message) {
		ProtocolHandler handler = handlerMap.get(message.getName());
		if(handler == null) {
			Channels.fireExceptionCaught(ctx.getChannel(), new RuntimeException("can not find handler for " + message.getName()));
		}
		
		handler.handleReceivedMessage(message);
		message.destory();
		handler.handleReply(ctx);
	}
}
