package pin.net;

import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.liteProto.LlpJava;
import com.liteProto.LlpMessage;

public class TestClientHandler extends SimpleChannelHandler {

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		sendReply(ctx);
	}

	private void sendReply(ChannelHandlerContext ctx) throws Exception {
		LlpMessage msg = LlpJava.instance().getMessage("testLlpDataType");
		msg.write("i32", 123);
		msg.write("i64", 456l);
		msg.write("f32", 123.1f);
		msg.write("f64", 345.2d);
		msg.write("str", "haha");
		byte[] data = msg.encode();
		byte[] msgName = "testLlpDataType".getBytes();
		int dataLen = data.length + msgName.length + 2;
		ChannelBuffer cb = ChannelBuffers.buffer(ByteOrder.LITTLE_ENDIAN,
				dataLen + 2);
		cb.writeShort(dataLen);
		cb.writeShort(msgName.length);
		cb.writeBytes(msgName);
		cb.writeBytes(data);
		ctx.getChannel().write(cb);
		msg.destory();
	}

	private void printRcv(LlpMessage msg) {
		int i32 = msg.readInt("i32");
		long i64 = msg.readLong("i64");
		float f32 = msg.readFloat("f32");
		double f64 = msg.readDouble("f64");
		String str = msg.readString("str");
		System.out.println("i32= " + i32 + " i64= " + i64 + " f32= " + f32
				+ " f64= " + f64 + " str= " + str);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		LlpMessage msg = (LlpMessage) e.getMessage();
		printRcv(msg);
		msg.destory();
		sendReply(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
	}

}
