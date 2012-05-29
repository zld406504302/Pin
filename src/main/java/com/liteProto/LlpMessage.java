package com.liteProto;

public class LlpMessage {
	private long llpMesHandle;
	private String name;

	public LlpMessage(long handle, String name) {
		this.llpMesHandle = handle;
		this.name = name;
	}

	public LlpMessage(long handle, String name, byte[] buff) {
		this(handle, name);
		this.decode(buff);
	}

	// destory
	public void destory() {
		LlpJavaNative.llpMessageFree(llpMesHandle);
		llpMesHandle = 0;
	}

	// clear
	public void clear() {
		LlpJavaNative.llpMessageClr(llpMesHandle);
	}

	public void write(String filedStr, int number) {
		if (filedStr == null)
			throw new NullPointerException();

		int ret = LlpJavaNative.llpWmesInt32(llpMesHandle, filedStr, number);
		if (ret == 0) {
			throw new RuntimeException("[LlpJavaNative WriteInt32]:  write message \"" + name + "\" filed \"" + filedStr + "\" number: " + number
					+ " is error.");
		}
	}

	public void write(String filedStr, boolean bool) {
		if (filedStr == null)
			throw new NullPointerException();

		int number = bool ? 0 : 1;
		int ret = LlpJavaNative.llpWmesInt32(llpMesHandle, filedStr, number);
		if (ret == 0) {
			throw new RuntimeException("[LlpJavaNative WriteInt32]:  write message \"" + name + "\" filed \"" + filedStr + "\" number: " + number
					+ " is error.");
		}
	}

	public void write(String filedStr, long number) {
		if (filedStr == null)
			throw new NullPointerException();

		int ret = LlpJavaNative.llpWmesInt64(llpMesHandle, filedStr, number);
		if (ret == 0) {
			throw new RuntimeException("[LlpJavaNative WriteInt64]:  write message \"" + name + "\" filed \"" + filedStr + "\" number: " + number
					+ " is error.");
		}
	}

	public void write(String filedStr, float number) {
		if (filedStr == null)
			throw new NullPointerException();

		int ret = LlpJavaNative.llpWmesFloat32(llpMesHandle, filedStr, number);
		if (ret == 0) {
			throw new RuntimeException("[LlpJavaNative Writefloat32]:  write message \"" + name + "\" filed \"" + filedStr + "\" number: " + number
					+ " is error.");
		}
	}

	public void write(String filedStr, double number) {
		if (filedStr == null)
			throw new NullPointerException();

		int ret = LlpJavaNative.llpWmesFloat64(llpMesHandle, filedStr, number);
		if (ret == 0) {
			throw new RuntimeException("[LlpJavaNative Writefloat64]:  write message \"" + name + "\" filed \"" + filedStr + "\" number: " + number
					+ " is error.");
		}
	}

	public void write(String filedStr, String str) {
		if (filedStr == null)
			throw new NullPointerException();

		if (str == null)
			return;

		int ret = LlpJavaNative.llpWmesString(llpMesHandle, filedStr, str);
		if (ret == 0) {
			throw new RuntimeException("[LlpJavaNative WriteString]:  write message \"" + name + "\" filed \"" + filedStr + "\" str: " + str
					+ " is error.");
		}
	}

	public void write(String filedStr, byte[] stream) {
		if (stream == null || filedStr == null)
			throw new NullPointerException();

		int ret = LlpJavaNative.llpWmesStream(llpMesHandle, filedStr, stream);
		if (ret == 0) {
			throw new RuntimeException("[LlpJavaNative WriteStream]:  write message \"" + name + "\" filed \"" + filedStr);
		}
	}

	public LlpMessage write(String filedStr) {
		if (filedStr == null)
			throw new NullPointerException();

		long handle = LlpJavaNative.llpWmesMessage(llpMesHandle, filedStr);
		if (handle == 0) {
			throw new RuntimeException("[LlpJavaNative writeMessage]:  write message \"" + name + "\" filed \"" + filedStr + "\"is error.");
		}

		return new LlpMessage(handle, filedStr);
	}

	public int readInt(String filedStr, int alInx) {
		if (filedStr == null)
			throw new NullPointerException();

		return LlpJavaNative.llpRmesInt32(llpMesHandle, filedStr, alInx);
	}

	public int readInt(String filedStr) {
		if (filedStr == null)
			throw new NullPointerException();

		return readInt(filedStr, 0);
	}

	public long readLong(String filedStr, int alInx) {
		if (filedStr == null)
			throw new NullPointerException();

		return LlpJavaNative.llpRmesInt64(llpMesHandle, filedStr, alInx);
	}

	public long readLong(String filedStr) {
		if (filedStr == null)
			throw new NullPointerException();

		return readLong(filedStr, 0);
	}

	public float readFloat(String filedStr, int alInx) {
		if (filedStr == null)
			throw new NullPointerException();

		return LlpJavaNative.llpRmesFloat32(llpMesHandle, filedStr, alInx);
	}

	public float readFloat(String filedStr) {
		if (filedStr == null)
			throw new NullPointerException();

		return readFloat(filedStr, 0);
	}

	public double readDouble(String filedStr, int alInx) {
		if (filedStr == null)
			throw new NullPointerException();

		return LlpJavaNative.llpRmesFloat64(llpMesHandle, filedStr, alInx);
	}

	public double readDouble(String filedStr) {
		if (filedStr == null)
			throw new NullPointerException();

		return readDouble(filedStr, 0);
	}

	public String readString(String filedStr, int alInx) {
		if (filedStr == null)
			throw new NullPointerException();

		byte[] str = LlpJavaNative.llpRmesString(llpMesHandle, filedStr, alInx);

		return new String(str);
	}

	public String readString(String filedStr) {
		return readString(filedStr, 0);
	}

	public byte[] readStream(String filedStr, int alInx) {
		if (filedStr == null)
			throw new NullPointerException();

		byte[] ret = LlpJavaNative.llpRmesStream(llpMesHandle, filedStr, alInx);
		return ret;
	}

	public byte[] readStream(String filedStr) {
		return readStream(filedStr, 0);
	}

	public LlpMessage readMessage(String filedStr, int alInx) {
		if (filedStr == null)
			throw new NullPointerException();

		long handle = LlpJavaNative.llpRmesMessage(llpMesHandle, filedStr, alInx);
		if (handle == 0)
			return null;

		return new LlpMessage(handle, filedStr);
	}

	public LlpMessage readMessage(String filedStr) throws Exception {
		return readMessage(filedStr, 0);
	}

	public int readSize(String filedStr) {
		if (filedStr == null)
			throw new NullPointerException();

		return LlpJavaNative.llpRmesSize(llpMesHandle, filedStr);
	}

	// 编码
	public byte[] encode() {
		return LlpJavaNative.llpOutMessage(llpMesHandle);
	}

	// 解码
	public void decode(byte[] buff) {
		this.clear();
		if (LlpJavaNative.llpInMessage(buff, llpMesHandle) == 0) {
			throw new RuntimeException("[LlpJavaNative decode]:  decode message \"" + name + "\" is error.");
		}
	}

	public String getName() {
		return name;
	}

}
