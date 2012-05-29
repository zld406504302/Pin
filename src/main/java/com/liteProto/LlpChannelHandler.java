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

public class LlpChannelHandler extends SimpleChannelHandler {
	private static Logger logger = LoggerFactory.getLogger(LlpChannelHandler.class);
	protected Map<String, ProtocolHandler> handlerMap = new HashMap<String, ProtocolHandler>();

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		LlpMessage msg = (LlpMessage) e.getMessage();
		try {
			ProtocolHandler handler = handlerMap.get(msg.getName());

			if (handler == null) {
				Channels.fireExceptionCaught(ctx.getChannel(), new RuntimeException("can not find handler for " + msg.getName()));
				return;
			}

			handler.preHandle(ctx);
			handler.handleReceived(ctx, msg);
			handler.handleReply(ctx);
			logger.debug("msg handled! :" + msg.getName());
		} finally {
			msg.destory();
		}
	}

	/**
	 * 协议名与{@link ProtocolHandler} 关联
	 * @param protocolName 协议名
	 * @param handler {@link ProtocolHandler}
	 */
	public void regProtocolHandler(String protocolName, ProtocolHandler handler) {
		handlerMap.put(protocolName, handler);
	}

}
