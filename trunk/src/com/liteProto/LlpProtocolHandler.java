package com.liteProto;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import pin.net.protocol.ProtocolHandler;

public abstract class LlpProtocolHandler implements ProtocolHandler {

	private ByteOrder order = ByteOrder.LITTLE_ENDIAN;

	public LlpProtocolHandler(ByteOrder order) {
		this.order = order;
	}

	public LlpProtocolHandler() {

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
		 ChannelFuture future = channel.write(cb);
		 future.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				System.out.println("sent...");
			}
		});
	}

}
