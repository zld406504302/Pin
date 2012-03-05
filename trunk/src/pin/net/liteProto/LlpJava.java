package pin.net.liteProto;
import java.lang.Exception;

public class LlpJava {
	private long env;

	public LlpJava() throws Exception {
		env = LlpJavaNative.llpNewEnv();
		if (env == 0) {
			throw new Exception("[LlpJavaNative Env]: newEnv is error.");
		}
	}

	public LlpJava(String[] regMes) throws Exception {
		this();
		for (int i = 0; i < regMes.length; i++) {
			this.regMessage(regMes[i]);
		}
	}

	public void destory() {
		LlpJavaNative.llpFreeEnv(env);
	}

	public void regMessage(String mes) throws Exception {
		if (LlpJavaNative.llpRegMes(env, mes) == 0) {
			throw new Exception("[LlpJavaNative RegMes]: regedit message \""
					+ mes + "\" is error.");
		}
	}

	public void delMessage(String mes) throws Exception {
		if (LlpJavaNative.llpDelMes(env, mes) == 0) {
			throw new Exception("[LlpJavaNative DelMes]: delete message \""
					+ mes + "\" is error.");
		}
	}

	public LlpMessage getMessage(String mes) {
		long handle = LlpJavaNative.llpMessageNew(env, mes);
		if (handle == 0) {
			throw new RuntimeException("[LlpJavaNative NewMes]: get message \""
					+ mes + "\" is error.");
		}

		LlpMessage llpMessage = new LlpMessage(handle, mes);
		return llpMessage;
	}

	public LlpMessage getMessage(String mes, byte[] buff) {
		long handle = LlpJavaNative.llpMessageNew(env, mes);
		if (handle == 0 || buff == null) {
			throw new RuntimeException("[LlpJavaNative NewMes]: get message \""
					+ mes + "\" is error.");
		}

		LlpMessage llpMessage = new LlpMessage(handle, mes, buff);
		return llpMessage;
	}
}
