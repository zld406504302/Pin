package com.liteProto;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

public final class LlpMessageSender {

	private ByteOrder order = ByteOrder.BIG_ENDIAN;

	/**
	 * 指定ChannelBuffer 字节序
	 * @param order {@link ByteOrder}
	 */
	public LlpMessageSender(ByteOrder order) {
		this.order = order;
	}

	/**
	 * 默认构造函数
	 */
	public LlpMessageSender() {

	}

	/**
	 * 发送数据
	 * @param channel {@link Channel}
	 * @param msgName 协议名
	 * @param data 协议数据
	 * @throws UnsupportedEncodingException {@link UnsupportedEncodingException}
	 */
	public void sendData(Channel channel, String msgName, byte[] data) throws UnsupportedEncodingException {
		int dataLen = data.length;
		byte[] msgNameData = msgName.getBytes("UTF-8");
		int msgNameLength = msgNameData.length;
		int totalLength = 2 + msgNameLength + dataLen;
		ChannelBuffer cb = ChannelBuffers.buffer(order, totalLength + 2);
		cb.writeShort(totalLength);
		cb.writeShort(msgNameLength);
		cb.writeBytes(msgNameData);
		cb.writeBytes(data);
		channel.write(cb);
	}

	/**
	 * 发送数据
	 * @param channel {@link Channel}
	 * @param msg 协议名
	 * @throws UnsupportedEncodingException {@link UnsupportedEncodingException}
	 */
	public void sendData(Channel channel, LlpMessage msg) throws UnsupportedEncodingException {
		sendData(channel, msg.getName(), msg.encode());
	}
}
