package pin.lua;

import java.util.ArrayList;
import java.util.List;

import org.keplerproject.luajava.LuaException;

public enum EJavaFunctions {

	LOG_INFO(new DefaultJavaFunc("_LOG_INFO") {

		@Override
		public int execute() throws LuaException {
			String msg = this.getParam(2).getString();
			Lua.LOGGER.info(msg);
			return 0;
		}
	}),

	LOG_ERROR(new DefaultJavaFunc("_LOG_ERROR") {

		@Override
		public int execute() throws LuaException {
			String msg = this.getParam(2).getString();
			Lua.LOGGER.error(msg);
			return 0;
		}
	}),

	LOG_WARN(new DefaultJavaFunc("_LOG_WARN") {

		@Override
		public int execute() throws LuaException {
			String msg = this.getParam(2).getString();
			Lua.LOGGER.warn(msg);
			return 0;
		}
	}),

	ObjectToString(new DefaultJavaFunc("_ObjectToString") {

		@Override
		public int execute() throws LuaException {
			Object obj = this.getParam(2).getObject();
			Lua.push(obj.toString());
			return 1;
		}
	});

	private DefaultJavaFunc javaFunc;

	/**
	 * 构造java函数枚举
	 * 
	 * @param javafunc
	 *            java函数
	 */
	private EJavaFunctions(final DefaultJavaFunc javafunc) {
		this.javaFunc = javafunc;
	}

	/**
	 * 得到枚举中的java函数列表
	 * 
	 * @return java函数列表
	 */
	public static List<DefaultJavaFunc> getJavaFuncs() {
		List<DefaultJavaFunc> funcs = new ArrayList<DefaultJavaFunc>();
		for (EJavaFunctions ejf : EJavaFunctions.values()) {
			funcs.add(ejf.javaFunc);
		}
		return funcs;
	}
}
