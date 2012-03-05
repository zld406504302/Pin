package pin.net.handler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class EchoHandler extends SimpleChannelHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		ChannelBuffer buf = (ChannelBuffer)e.getMessage();
		
		
		byte b = buf.readByte();
		short s = buf.readShort();
		int i = buf.readInt();
		long l = buf.readLong();
		System.out.println("byte = " + b + " short " + s + " int " + i + " long " + l);
	}

}
