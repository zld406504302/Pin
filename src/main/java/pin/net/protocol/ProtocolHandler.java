package pin.net.protocol;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.liteProto.LlpMessage;

public interface ProtocolHandler {
	/**
	 * 预处理
	 * 
	 * @param ctx
	 *            {@link ChannelHandlerContext} 上下文
	 */
	public void preHandle(ChannelHandlerContext ctx);

	/**
	 * 接收处理
	 * 
	 * @param ctx
	 *            {@link ChannelHandlerContext} 上下文
	 * @param msg
	 *            接收到得消息
	 */
	public void handleReceived(ChannelHandlerContext ctx, LlpMessage msg);

	/**
	 * 发送处理
	 * 
	 * @param ctx
	 *            {@link ChannelHandlerContext} 上下文
	 * @throws Exception
	 *             异常信息
	 */
	public void handleReply(ChannelHandlerContext ctx) throws Exception;
}
