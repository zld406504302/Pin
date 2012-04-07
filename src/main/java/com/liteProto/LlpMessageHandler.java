package com.liteProto;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

import pin.net.protocol.ProtocolHandler;

public abstract class LlpMessageHandler implements ProtocolHandler {

	private ByteOrder order = ByteOrder.BIG_ENDIAN;

	public LlpMessageHandler(ByteOrder order) {
		this.order = order;
	}

	public LlpMessageHandler() {

	}

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

	public void sendData(Channel channel, LlpMessage msg) throws UnsupportedEncodingException {
		sendData(channel, msg.getName(), msg.encode());
	}
}
