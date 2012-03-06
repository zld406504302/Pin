package pin.net.liteProto;
import java.lang.Exception;

public class LlpJava {
	private long env;
	private static LlpJava instance = new LlpJava();

	private LlpJava() {
		env = LlpJavaNative.llpNewEnv();
		if (env == 0) {
			throw new RuntimeException("[LlpJavaNative Env]: newEnv is error.");
		}
	}
	
	public static LlpJava instance() {
		return instance;
	}

	public void destory() {
		LlpJavaNative.llpFreeEnv(env);
	}

	public void regMessage(String[] regMsg) throws Exception {
		for (int i = 0; i < regMsg.length; i++) {
			this.regMessage(regMsg[i]);
		}
	}
	
	public void regMessage(String msg) throws Exception {
		if (LlpJavaNative.llpRegMes(env, msg) == 0) {
			throw new Exception("[LlpJavaNative RegMes]: regedit message \""
					+ msg + "\" is error.");
		}
	}

	/**
	 * 此接口比较危险 尽量不要使用
	 * 如果调用此方法所有使用关于此lpb文件的message对象都会抛出异常
	 * @param lpbfile 要删除的lpb文件名
	 * @throws Exception
	 */
	public void delMessage(String lpbfile) throws Exception {
		if (LlpJavaNative.llpDelMes(env, lpbfile) == 0) {
			throw new Exception("[LlpJavaNative DelMes]: delete message \""
					+ lpbfile + "\" is error.");
		}
	}

	public LlpMessage getMessage(String msg) throws Exception {
		long handle = LlpJavaNative.llpMessageNew(env, msg);
		if (handle == 0) {
			throw new Exception("[LlpJavaNative NewMes]: get message \""
					+ msg + "\" is error.");
		}

		LlpMessage llpMessage = new LlpMessage(handle, msg);
		return llpMessage;
	}

	public LlpMessage getMessage(String msg, byte[] buff) throws Exception {
		long handle = LlpJavaNative.llpMessageNew(env, msg);
		if (handle == 0 || buff == null) {
			throw new Exception("[LlpJavaNative NewMes]: get message \""
					+ msg + "\" is error.");
		}

		LlpMessage llpMessage = new LlpMessage(handle, msg, buff);
		return llpMessage;
	}
}
