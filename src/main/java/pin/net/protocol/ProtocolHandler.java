package pin.net.protocol;

import org.jboss.netty.channel.Channel;

import com.liteProto.LlpMessage;

public interface ProtocolHandler {
	/**
	 * 预处理
	 * 
	 * @param channelId
	 *            Returns the unique String ID of this channel.
	 */
	void preHandle(Channel channel);

	/**
	 * 接收处理
	 * 
	 * @param msg
	 *            接收到得消息
	 */
	void handleReceived(LlpMessage msg);

	/**
	 * 发送处理
	 * 
	 * @throws Exception
	 *             异常信息
	 */
	void handleReply() throws Exception;
}
