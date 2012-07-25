package com.liteProto;

import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import static org.jboss.netty.channel.Channels.*;

public class LlpSender extends SimpleChannelHandler {

	private ByteOrder order = ByteOrder.BIG_ENDIAN;

	/**
	 * 指定ChannelBuffer 字节序
	 * 
	 * @param order
	 *            {@link ByteOrder}
	 */
	public LlpSender(ByteOrder order) {
		this.order = order;
	}

	/**
	 * 默认构造函数
	 */
	public LlpSender() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelHandler#writeRequested(org.jboss
	 * .netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		LlpMessage llpMessage = (LlpMessage) e.getMessage();
		byte[] data = llpMessage.encode();
		int dataLen = data.length;
		byte[] msgNameData = llpMessage.getName().getBytes("UTF-8");
		int msgNameLength = msgNameData.length;
		int totalLength = 2 + msgNameLength + dataLen;
		ChannelBuffer cb = ChannelBuffers.buffer(order, totalLength + 2);
		cb.writeShort(totalLength);
		cb.writeShort(msgNameLength);
		cb.writeBytes(msgNameData);
		cb.writeBytes(data);
		write(ctx, e.getFuture(), cb);
	}

}
