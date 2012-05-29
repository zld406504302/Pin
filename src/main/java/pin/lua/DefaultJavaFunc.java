package pin.lua;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultJavaFunc extends JavaFunction {

	protected String funcName;
	protected Logger logger = LoggerFactory.getLogger(DefaultJavaFunc.class);

	/**
	 * 构造默认java函数
	 * 
	 * @param funcName
	 *            lua中java函数名称
	 */
	public DefaultJavaFunc(String funcName) {
		super(null);
		this.funcName = funcName;
	}

	/**
	 * 向lua中注册java函数
	 */
	public void register() {
		try {
			this.L = Lua.lua;
			super.register(funcName);
		} catch (LuaException e) {
			logger.error("注册Java函数" + funcName + "失败！", e);
		}
	}
}
