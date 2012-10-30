package com.liteProto;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pin.net.protocol.ProtocolHandler;
import pin.spring.Spring;

public class LlpChannelHandler extends SimpleChannelHandler {
	private static Logger logger = LoggerFactory.getLogger(LlpChannelHandler.class);
	protected Map<String, ProtocolHandler> handlerMap = new HashMap<String, ProtocolHandler>();

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		LlpMessage msg = (LlpMessage) e.getMessage();
		try {
			ProtocolHandler handler = null;
			if (handlerMap.containsKey(msg.getName())) {
				handler = handlerMap.get(msg.getName());
			} else {
				// Spring工厂得到Handler对象
				handler = Spring.instance().getBean(msg.getName(), ProtocolHandler.class);
				handlerMap.put(msg.getName(), handler);
			}

			if (handler == null) {
				Channels.fireExceptionCaught(ctx.getChannel(), new RuntimeException("can not find handler for " + msg.getName()));
				return;
			}

			handler.preHandle(ctx.getChannel());
			handler.handleReceived(msg);
			handler.handleReply();
			logger.debug("msg handled! :" + msg.getName());
		} finally {
			msg.destory();
		}
	}
}
