package pin.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Pin {

	public static final boolean IS_LINUX = System.getProperty("os.name").contains("Linux");
	public static final boolean IS_64BIT = System.getProperty("os.arch").equals("amd64");
	public static final long START_TIME = System.currentTimeMillis();
	private static final Logger LOGGER = LoggerFactory.getLogger(Pin.class);
	private static final int BUFFER_SIZE = 4096;

	/**
	 * 私有构造函数
	 */
	private Pin() {

	}

	/**
	 * 根据不同系统加载库文件 如果库文件不存在则从jar包中解压出来
	 * 
	 * @param libName
	 *            库名称
	 */
	public static void loadLibrary(String libName) {
		String lib = libName + (IS_64BIT ? "-64" : "");
		String libFileName = (IS_LINUX ? "lib" : "") + lib + (IS_LINUX ? ".so" : ".dll");
		File nativeFile = new File(libFileName);

		InputStream input = null;
		FileOutputStream output = null;
		if (!nativeFile.exists()) {
			try {
				// Extract native from classpath to native dir.
				input = Pin.class.getResourceAsStream("/natives/" + libFileName);
				if (input == null) {
					throw new RuntimeException("cannot find native file: " + libFileName);
				}

				output = new FileOutputStream(nativeFile);
				byte[] buffer = new byte[BUFFER_SIZE];
				while (true) {
					int length = input.read(buffer);
					if (length == -1) {
						break;
					}
					output.write(buffer, 0, length);
				}
				input.close();
				output.close();
			} catch (IOException ex) {
				LOGGER.error("error on extracting libs", ex);
			} finally {
				try {
					if (input != null) {
						input.close();
					}
					if (output != null) {
						output.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.loadLibrary(lib);
	}
}
