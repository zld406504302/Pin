package pin.net.liteProto;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Э���ʽΪ: frameLength(short) + msgName(utf) + llpMsg
 * 
 * @author zhongyuan
 * 
 */
public class LlpDecoder extends FrameDecoder {

	private static final Logger logger = LoggerFactory
			.getLogger(LlpDecoder.class);

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {

		if (buffer.readableBytes() < 2) {
			return null;
		}

		// ��ȡframe�����ֶ�
		short frameLength = buffer.getShort(buffer.readerIndex());

		if (frameLength <= 4) {
			buffer.skipBytes(2);// ���ȱ�ʶС��0 ��������
			Channels.fireExceptionCaught(channel, new CorruptedFrameException(
					"negative length field: " + frameLength));
		}

		// �ж�length���ȵĺ�����һ��Э�鳤������Ϊ4(frameLength + utfLength)����С��Short.MAX_VALUE
		if (frameLength > Short.MAX_VALUE) { // ���ȴ��ں���ֵ�رմ�����
			Channels.fireExceptionCaught(channel, new TooLongFrameException(
					"frame length exceeds " + Short.MAX_VALUE));
		}

		if (buffer.readableBytes() < frameLength + 2) { // +2Ϊ����frameLength����short�ĳ���
			return null;
		}

		LlpMessage msg = getLlpMessage(channel, buffer, buffer.readerIndex(),
				frameLength);
		buffer.readerIndex(buffer.readerIndex() + frameLength + 2); // ��������һ����Ϣ
																	// length(short)
																	// + data
		return msg;
	}

	/**
	 * ��ChannelBuffer�л�ȡLlpMessage ChannelBuffer�����ݸ�ʽΪ msgName(utf) + llpMsg
	 * 
	 * @param buffer
	 *            ���緢�͹�����LlpMessage����
	 * @param index
	 *            buffer��ʼ��ȡλ��
	 * @param length
	 *            msgName(utf) + llpMsg�ܳ���
	 * @return LlpMessage
	 */
	private LlpMessage getLlpMessage(Channel channel, ChannelBuffer buffer,
			int index, int length) {
		// ��ȡUTF Э������
		short strLen = buffer.getShort(index);
		String protocolName = getUTFStr(buffer, index, strLen);
		if (protocolName == null) {
			Channels.fireExceptionCaught(channel, new CorruptedFrameException(
					"uft encoding error!"));
			return null;
		}
		// ����llpMessage
		int msgLen = length - strLen - 2; // msgLen = length - utfLen
		int msgStartPos = index + strLen + 2;
		ChannelBuffer frame = buffer.factory().getBuffer(msgLen);
		frame.writeBytes(buffer, msgStartPos, msgLen);
		LlpMessage msg = null;
		try {
			msg = LlpJava.instance().getMessage(protocolName, frame.array());
		} catch (Exception e) {
			Channels.fireExceptionCaught(channel, new CorruptedFrameException(
					"phrase llp message failed!"));
		}
		return msg;
	}

	private String getUTFStr(ChannelBuffer buffer, int index, int strLen) {
		if (strLen > 0) {
			byte[] data = new byte[strLen];
			buffer.getBytes(index, data, index + 2, strLen);
			try {
				return new String(data, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("uft encoding error!", e);
				return null;
			}
		}

		return null;
	}
}
