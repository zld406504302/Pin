package com.liteProto;

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

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		LlpMessage msg = (LlpMessage) e.getMessage();
		try {
			//Spring工厂得到Handler对象
			ProtocolHandler handler = Spring.instance().getBean(msg.getName(), ProtocolHandler.class);

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
}
