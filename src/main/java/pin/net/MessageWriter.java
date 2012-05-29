package pin.net;

import com.liteProto.LlpMessage;

public interface MessageWriter {
	/**
	 * 填充网络协议
	 * 
	 * @param msg
	 *            网络协议
	 */
	void write(LlpMessage msg);
}
