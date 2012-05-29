package pin.lua;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pin.core.Pin;

/**
 * 封装对Lua的访问
 * 
 * @author Sid
 * 
 */
public final class Lua {
	/** 全局唯一LuaState */
	static LuaState lua = null;

	/** 函数性能统计 */
	private static HashMap<String, Stat> statinfo = new HashMap<String, Stat>();

	public static final Logger LOGGER = LoggerFactory.getLogger(Lua.class);

	static {
		Pin.loadLibrary("luajava");
	}

	/** 私有构造函数，不可实例化 */
	private Lua() {

	}

	/**
	 * 启动脚本服务
	 * 
	 * @param luaDirectory
	 *            程序脚本目录
	 * @param javaFuncs
	 *            java函数
	 */
	public static void start(final File luaDirectory, final List<DefaultJavaFunc> javaFuncs) {
		init();
		registerJavaFuncs(javaFuncs);
		loadLua(luaDirectory);
	}

	/**
	 * 初始化lua
	 */
	private static void init() {
		lua = LuaStateFactory.newLuaState();
		lua.checkStack(1024);
		lua.openLibs();
	}

	/**
	 * 关闭Lua应用
	 */
	public static synchronized void close() {

		synchronized (lua) {
			if (!lua.isClosed()) {
				lua.close();
			}
		}
	}

	/**
	 * 向lua注册java函数
	 * 
	 * @param javaFuncs
	 *            java函数
	 */
	private static void registerJavaFuncs(final List<DefaultJavaFunc> javaFuncs) {
		for (DefaultJavaFunc jf : javaFuncs) {
			jf.register();
		}
	}

	/**
	 * 加载脚本
	 * 
	 * @param luaDirectory
	 *            lua文件夹
	 */
	private static void loadLua(File luaDirectory) {
		LOGGER.info("～～～加载脚本～～～");

		if (lua.LdoFile(luaDirectory.getAbsolutePath() + File.separator + "load.lua") != 0) {
			LOGGER.error("加载load.lua失败");
			LOGGER.info("～～～加载完成～～～");
			return;
		}

		for (String directory : call4String("loadLua").split(";")) {
			LOGGER.info(luaDirectory.getPath() + "/" + directory);
			loadDirectory(new File(luaDirectory.getAbsolutePath() + File.separator + directory));
		}

		LOGGER.info("～～～加载完成～～～");
	}

	/**
	 * 加载指定目录中所有的脚本，先加载当前目录下的脚本，然后递归加载此子目录
	 * 
	 * @param directory
	 *            脚本文件目录
	 */
	private static void loadDirectory(final File directory) {
		for (File luaScript : directory.listFiles(luaFilter)) {
			if (lua.LdoFile(luaScript.getAbsolutePath()) != 0) {
				LOGGER.error("加载【" + luaScript.getAbsolutePath() + "】失败！");
			} else {
				LOGGER.info("加载【" + luaScript.getAbsolutePath() + "】成功！");
			}
		}

		for (File luaDirectory : directory.listFiles(directoryFilter)) {
			if (!luaDirectory.isHidden()) {
				loadDirectory(luaDirectory);
			}
		}
	}

	private static FileFilter luaFilter = new FileFilter() {

		public boolean accept(File file) {
			return file.isFile() && file.getName().endsWith(".lua");
		}

	};

	private static FileFilter directoryFilter = new FileFilter() {

		public boolean accept(File file) {
			return file.isDirectory();
		}

	};

	/**
	 * 向栈中压入数据
	 * 
	 * @param obj
	 *            目标数据
	 */
	public static void push(final Object obj) {
		if (obj == null) {
			lua.pushNil();
		} else if (obj instanceof String) {
			lua.pushString((String) obj);
		} else if (obj instanceof Number) {
			lua.pushNumber(((Number) obj).doubleValue());
		} else if (obj instanceof Boolean) {
			lua.pushBoolean((Boolean) obj);
		} else {
			lua.pushJavaObject(obj);
		}
	}

	/**
	 * 转换LuaObject为指定类型的数据
	 * 
	 * @param <T>
	 *            模板参数
	 * @param c
	 *            目标类型的.class，便于做类型转换保证
	 * @param lobj
	 *            待转换的数据
	 * @return 目标数据
	 */
	@SuppressWarnings("unchecked")
	private static <T> T castData(final Class<T> c, final LuaObject lobj) {
		if (c == null) {
			if (lobj.isNil()) {
				return null;
			} else {
				try {
					return (T) lobj.getObject();
				} catch (LuaException e1) {
					throw new ClassCastException(lobj + " is not java object");
				}
			}
		}

		if (c == Boolean.class) {
			if (lobj.isNil()) {
				return c.cast(false);
			}

			if (lobj.isBoolean()) {
				return c.cast(lobj.getBoolean());
			}

			return c.cast(true);
		}

		if (lobj.isNil()) {
			return null;
		}

		if (c == Byte.class && lobj.isNumber()) {
			return c.cast((byte) lobj.getNumber());
		} else if (c == Short.class && lobj.isNumber()) {
			return c.cast((short) lobj.getNumber());
		} else if (c == Integer.class && lobj.isNumber()) {
			return c.cast((int) lobj.getNumber());
		} else if (c == Long.class && lobj.isNumber()) {
			return c.cast((long) lobj.getNumber());
		} else if (c == Float.class && lobj.isNumber()) {
			return c.cast((float) lobj.getNumber());
		} else if (c == Double.class && lobj.isNumber()) {
			return c.cast(lobj.getNumber());
		} else if (c == String.class && lobj.isString()) {
			return c.cast(lobj.getString());
		} else if (lobj.isUserdata() || lobj.isJavaObject()) {
			try {
				return c.cast(lobj.getObject());
			} catch (LuaException e) {
				e.printStackTrace();
				return null;
			}
		}

		throw new ClassCastException(lobj + " is not instance of " + c);
	}

	/**
	 * 获取Table属性
	 * 
	 * @param <T>
	 *            模板参数
	 * @param c
	 *            返回值类型的.class，便于做类型转换保证
	 * @param indexs
	 *            索引
	 * @return Table属性
	 */
	public static <T> T getObject(final Class<T> c, final Object... indexs) {
		LuaObject lobj = null;
		synchronized (lua) {
			if (lua.isClosed()) {
				Thread.dumpStack();
			}

			int stackTop = lua.getTop();

			lua.getGlobal("_G");

			for (int i = 1; i <= indexs.length; i++) {
				if (lua.isTable(-1)) {
					push(indexs[i - 1]);
					lua.getTable(-2);
				} else {
					lua.setTop(stackTop);
					return null;
				}
			}

			lobj = lua.getLuaObject(-1);

			lua.pop(-1 - indexs.length);

			lua.setTop(stackTop);
		}

		return castData(c, lobj);
	}

	/**
	 * 调用Lua函数
	 * 
	 * @param <T>
	 *            模板参数
	 * @param oocall
	 *            是否是面向对象函数调用，面向对象调用会自动添加self为函数的第一个参数
	 * @param indexNum
	 *            索引的个数
	 * @param result
	 *            是否有返回值，由于Java中仅支持单返回值，这里没有做扩展
	 * @param c
	 *            返回值类型的.class，便于做类型转换保证。为null则返回Object。
	 * @param indexAndArgs
	 *            可变参数部分，依次填写索引部分、要调用的函数名、函数参数
	 * @return Lua函数的返回值
	 */
	private static <T> T callLua(final boolean oocall, final int indexNum, final boolean result, final Class<T> c, final Object... indexAndArgs) {
		if (indexAndArgs.length <= indexNum) {
			throw new IllegalArgumentException("参数长度必须大于索引数目！");
		}

		if (!(indexAndArgs[indexNum] instanceof String)) {
			StringBuilder builder = new StringBuilder();
			builder.append("\n面向对象调用 = " + oocall + "/n");
			builder.append("索引的个数 = " + indexNum + "/n");
			builder.append("返回值 = " + result + "/n");
			builder.append("期待返回类型 = " + c + "/n");
			builder.append("可变参数部分：");
			for (Object arg : indexAndArgs) {
				builder.append(arg + " ");
			}

			throw new IllegalArgumentException("函数名必须为String！" + builder);
		}

		long call = System.nanoTime();

		String function = (String) indexAndArgs[indexNum];

		LuaObject lobj = null;

		synchronized (lua) {
			if (lua.isClosed()) {
				Thread.dumpStack();
			}

			int stackTop = lua.getTop();

			lua.getGlobal("traceback");

			lua.getGlobal("_G");

			for (int i = 1; i <= indexNum; i++) {
				push(indexAndArgs[i - 1]);
				lua.getTable(-2);

				if (!lua.isTable(-1)) {
					lua.setTop(stackTop);
					throw new IllegalArgumentException("不是table，无法调用函数");
				}
			}

			lua.getField(-1, function);

			if (!lua.isFunction(-1) && !lua.isJavaFunction(-1)) {
				lua.setTop(stackTop);
				StringBuilder builder = new StringBuilder();

				builder.append("_G");
				for (int i = 1; i <= indexNum; i++) {
					builder.append("[" + indexAndArgs[i - 1] + "]");
				}
				builder.append(".");
				builder.append(function);
				builder.append("属性不是函数，无法调用");
				throw new IllegalArgumentException(builder.toString());
			}

			if (oocall) {
				lua.pushValue(-2);
			}

			for (int i = indexNum + 1; i < indexAndArgs.length; i++) {
				push(indexAndArgs[i]);
			}

			if (lua.pcall(indexAndArgs.length - indexNum - (oocall ? 0 : 1), result ? 1 : 0, stackTop + 1) != 0) {
				LOGGER.error(lua.getLuaObject(-1).getString());
			} else if (result) {
				lobj = lua.getLuaObject(-1);
			}

			lua.setTop(stackTop);
		}

		long time = (System.nanoTime() - call) / 1000000;
		Stat stat = statinfo.get(function);
		if (stat == null) {
			stat = new Stat(1, time);
			statinfo.put(function, stat);
		} else {
			stat.count++;
			stat.timecost += time;
		}

		return result ? castData(c, lobj) : null;
	}

	/**
	 * 获取boolen类型的属性
	 * 
	 * @param indexs
	 *            从_G往下的索引
	 * @return lua boolean<br>
	 *         (lua nil = false, lua false = false, lua other = true)
	 */
	public static boolean getBool(final Object... indexs) {
		return getObject(Boolean.class, indexs);
	}

	/**
	 * 获取byte类型的属性
	 * 
	 * @param indexs
	 *            从_G往下的索引
	 * @return (byte)(lua number)
	 */
	public static byte getByte(final Object... indexs) {
		return getObject(Byte.class, indexs);
	}

	/**
	 * 获取short类型的属性
	 * 
	 * @param indexs
	 *            从_G往下的索引
	 * @return (short)(lua number)
	 */
	public static short getShort(final Object... indexs) {
		return getObject(Short.class, indexs);
	}

	/**
	 * 获取int类型的属性
	 * 
	 * @param indexs
	 *            从_G往下的索引
	 * @return (int)(lua number)
	 */
	public static int getInt(final Object... indexs) {
		return getObject(Integer.class, indexs);
	}

	/**
	 * 获取long类型的属性
	 * 
	 * @param indexs
	 *            从_G往下的索引
	 * @return (long)(lua number)
	 */
	public static long getLong(final Object... indexs) {
		return getObject(Long.class, indexs);
	}

	/**
	 * 获取float类型的属性
	 * 
	 * @param indexs
	 *            从_G往下的索引
	 * @return (float)(lua number)
	 */
	public static float getFloat(final Object... indexs) {
		return getObject(Float.class, indexs);
	}

	/**
	 * 获取double类型的属性
	 * 
	 * @param indexs
	 *            从_G往下的索引
	 * @return (double)(lua number)
	 */
	public static double getDouble(final Object... indexs) {
		return getObject(Double.class, indexs);
	}

	/**
	 * 获取String类型的属性
	 * 
	 * @param indexs
	 *            从_G往下的索引
	 * @return lua string
	 */
	public static String getString(final Object... indexs) {
		return getObject(String.class, indexs);
	}

	/**
	 * 调用lua函数
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 */
	public static void call(final int indexNum, final Object... indexAndArgs) {
		callLua(false, indexNum, false, null, indexAndArgs);
	}

	/**
	 * 调用lua全局函数
	 * 
	 * @param args
	 *            函数名+参数，函数名必须为String
	 */
	public static void callLuaFunction(final Object... args) {
		callLua(false, 0, false, null, args);
	}

	/**
	 * 调用lua函数，期待返回值为boolean
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return lua boolean
	 */
	public static boolean call4Bool(final int indexNum, final Object... indexAndArgs) {
		return callLua(false, indexNum, true, Boolean.class, indexAndArgs);
	}

	/**
	 * 调用lua函数，期待返回值为byte
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (byte)(lua number)
	 */
	public static byte call4Byte(final int indexNum, final Object... indexAndArgs) {
		return callLua(false, indexNum, true, Byte.class, indexAndArgs);
	}

	/**
	 * 调用lua函数，期待返回值为short
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (short)(lua number)
	 */
	public static short call4Short(final int indexNum, final Object... indexAndArgs) {
		return callLua(false, indexNum, true, Short.class, indexAndArgs);
	}

	/**
	 * 调用lua函数，期待返回值为int
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (int)(lua number)
	 */
	public static int call4Int(final int indexNum, final Object... indexAndArgs) {
		return callLua(false, indexNum, true, Integer.class, indexAndArgs);
	}

	/**
	 * 调用lua函数，期待返回值为long
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (long)(lua number)
	 */
	public static long call4Long(final int indexNum, final Object... indexAndArgs) {
		return callLua(false, indexNum, true, Long.class, indexAndArgs);
	}

	/**
	 * 调用lua函数，期待返回值为float
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (float)(lua number)
	 */
	public static float call4Float(final int indexNum, final Object... indexAndArgs) {
		return callLua(false, indexNum, true, Float.class, indexAndArgs);
	}

	/**
	 * 调用lua函数，期待返回值为double
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (double)(lua number)
	 */
	public static double call4Double(final int indexNum, final Object... indexAndArgs) {
		return callLua(false, indexNum, true, Double.class, indexAndArgs);
	}

	/**
	 * 调用lua函数，期待返回值为String
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return lua string
	 */
	public static String call4String(final int indexNum, final Object... indexAndArgs) {
		return callLua(false, indexNum, true, String.class, indexAndArgs);
	}

	/**
	 * 调用lua函数，返回模板参数类型的对象
	 * 
	 * @param <T>
	 *            模板参数
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return 模板参数类型的对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T call4Object(final int indexNum, final Object... indexAndArgs) {
		return (T) callLua(false, indexNum, true, null, indexAndArgs);
	}

	/**
	 * 调用全局lua函数，期待返回值为boolean
	 * 
	 * @param args
	 *            函数名+参数，函数名必须为String
	 * @return lua boolean
	 */
	public static boolean call4Bool(final Object... args) {
		return callLua(false, 0, true, Boolean.class, args);
	}

	/**
	 * 调用全局lua函数，期待返回值为byte
	 * 
	 * @param args
	 *            函数名+参数，函数名必须为String
	 * @return (byte)(lua number)
	 */
	public static byte call4Byte(final Object... args) {
		return callLua(false, 0, true, Byte.class, args);
	}

	/**
	 * 调用全局lua函数，期待返回值为short
	 * 
	 * @param args
	 *            函数名+参数，函数名必须为String
	 * @return (short)(lua number)
	 */
	public static short call4Short(final Object... args) {
		return callLua(false, 0, true, Short.class, args);
	}

	/**
	 * 调用全局lua函数，期待返回值为int
	 * 
	 * @param args
	 *            函数名+参数，函数名必须为String
	 * @return (int)(lua number)
	 */
	public static int call4Int(final Object... args) {
		return callLua(false, 0, true, Integer.class, args);
	}

	/**
	 * 调用全局lua函数，期待返回值为long
	 * 
	 * @param args
	 *            函数名+参数，函数名必须为String
	 * @return (long)(lua number)
	 */
	public static long call4Long(final Object... args) {
		return callLua(false, 0, true, Long.class, args);
	}

	/**
	 * 调用全局lua函数，期待返回值为float
	 * 
	 * @param args
	 *            函数名+参数，函数名必须为String
	 * @return (float)(lua number)
	 */
	public static float call4Float(final Object... args) {
		return callLua(false, 0, true, Float.class, args);
	}

	/**
	 * 调用全局lua函数，期待返回值为double
	 * 
	 * @param args
	 *            函数名+参数，函数名必须为String
	 * @return (double)(lua number)
	 */
	public static double call4Double(final Object... args) {
		return callLua(false, 0, true, Double.class, args);
	}

	/**
	 * 调用全局lua函数，期待返回值为String
	 * 
	 * @param args
	 *            函数名+参数，函数名必须为String
	 * @return lua string
	 */
	public static String call4String(final Object... args) {
		return callLua(false, 0, true, String.class, args);
	}

	/**
	 * 调用全局lua函数，期待返回值为ArrayList<T>
	 * 
	 * @param <T>
	 *            模板参数
	 * @param args
	 *            函数名+参数，函数名必须为String
	 * @return ArrayList<T>
	 */
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> call4List(final Object... args) {
		return (ArrayList<T>) callLua(false, 0, true, null, args);
	}

	/**
	 * 调用全局lua函数，返回模板参数类型的对象
	 * 
	 * @param <T>
	 *            模板参数
	 * @param args
	 *            函数名+参数，函数名必须为String
	 * @return 模板参数类型的对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T call4Object(final Object... args) {
		return (T) callLua(false, 0, true, null, args);
	}

	/**
	 * 调用面向对象lua函数
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 */
	public static void callOO(final int indexNum, final Object... indexAndArgs) {
		callLua(true, indexNum, false, null, indexAndArgs);
	}

	/**
	 * 调用面向对象lua函数，期待返回值为boolean
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return lua boolean
	 */
	public static boolean callOO4Bool(final int indexNum, final Object... indexAndArgs) {
		return callLua(true, indexNum, true, Boolean.class, indexAndArgs);
	}

	/**
	 * 调用面向对象lua函数，期待返回值为byte
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (byte)(lua number)
	 */
	public static byte callOO4Byte(final int indexNum, final Object... indexAndArgs) {
		return callLua(true, indexNum, true, Byte.class, indexAndArgs);
	}

	/**
	 * 调用面向对象lua函数，期待返回值为short
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (short)(lua number)
	 */
	public static short callOO4Short(final int indexNum, final Object... indexAndArgs) {
		return callLua(true, indexNum, true, Short.class, indexAndArgs);
	}

	/**
	 * 调用面向对象lua函数，期待返回值为int
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (int)(lua number)
	 */
	public static int callOO4Int(final int indexNum, final Object... indexAndArgs) {
		return callLua(true, indexNum, true, Integer.class, indexAndArgs);
	}

	/**
	 * 调用面向对象lua函数，期待返回值为long
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (long)(lua number)
	 */
	public static long callOO4Long(final int indexNum, final Object... indexAndArgs) {
		return callLua(true, indexNum, true, Long.class, indexAndArgs);
	}

	/**
	 * 调用面向对象lua函数，期待返回值为float
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (float)(lua number)
	 */
	public static float callOO4Float(final int indexNum, final Object... indexAndArgs) {
		return callLua(true, indexNum, true, Float.class, indexAndArgs);
	}

	/**
	 * 调用面向对象lua函数，期待返回值为double
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return (double)(lua number)
	 */
	public static double callOO4Double(final int indexNum, final Object... indexAndArgs) {
		return callLua(true, indexNum, true, Double.class, indexAndArgs);
	}

	/**
	 * 调用面向对象lua函数，期待返回值为String
	 * 
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return lua string
	 */
	public static String callOO4String(final int indexNum, final Object... indexAndArgs) {
		return callLua(true, indexNum, true, String.class, indexAndArgs);
	}

	/**
	 * 调用面向对象lua函数，返回模板参数类型的对象
	 * 
	 * @param <T>
	 *            模板参数
	 * @param indexNum
	 *            indexAndArgs中从_G往下的索引个数
	 * @param indexAndArgs
	 *            索引+函数名+参数
	 * @return 模板参数类型的对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T callOO4Object(final int indexNum, final Object... indexAndArgs) {
		return (T) callLua(true, indexNum, true, null, indexAndArgs);
	}

	/**
	 * Lua函数性能统计
	 * 
	 * @author Sid
	 */
	private static class Stat {
		/** 调用次数 */
		private int count;
		/** 耗费时间 */
		private long timecost;

		/**
		 * @param count
		 *            调用次数
		 * @param timecost
		 *            耗费时间
		 */
		public Stat(final int count, final long timecost) {
			this.count = count;
			this.timecost = timecost;
		}

		@Override
		public String toString() {
			return "  调用次数：" + count + "  总耗时：" + timecost + "毫秒  平均耗时：" + timecost / (double) count + "毫秒";
		}
	}

	/**
	 * 输出Lua函数性能统计结果
	 */
	public static void funcchk() {
		for (Entry<String, Stat> entry : statinfo.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
		}
	}

	/** 清空统计性息 */
	public static void resetStat() {
		statinfo.clear();
	}

}
