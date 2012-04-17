package pin.lua;


import java.util.ArrayList;
import java.util.List;

import org.keplerproject.luajava.LuaException;


public enum EJavaFunctions {
	
	
	LOG_INFO(new DefaultJavaFunc("_LOG_INFO") {

		@Override
		public int execute() throws LuaException {
			String msg = this.getParam(2).getString();
			Lua.logger.info(msg);
			return 0;
		}
	}),
	
	LOG_ERROR(new DefaultJavaFunc("_LOG_ERROR") {

		@Override
		public int execute() throws LuaException {
			String msg = this.getParam(2).getString();
			Lua.logger.error(msg);
			return 0;
		}
	}),
	
	LOG_WARN(new DefaultJavaFunc("_LOG_WARN") {

		@Override
		public int execute() throws LuaException {
			String msg = this.getParam(2).getString();
			Lua.logger.warn(msg);
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
	}),
	
	;
	private DefaultJavaFunc javaFunc;

	private EJavaFunctions(final DefaultJavaFunc javafunc) {
		this.javaFunc = javafunc;
	}
	
	public static List<DefaultJavaFunc> getJavaFuncs() {
		List<DefaultJavaFunc> funcs = new ArrayList<DefaultJavaFunc>();
		for(EJavaFunctions ejf : EJavaFunctions.values()) {
			funcs.add(ejf.javaFunc);
		}
		return funcs;
	}
}
