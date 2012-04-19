package pin.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Pin {

	public static final boolean isLinux = System.getProperty("os.name").contains("Linux");
	public static final boolean is64Bit = System.getProperty("os.arch").equals("amd64");
	public static final long startTime = System.currentTimeMillis();

	public static void loadLibrary(String libName) {
		String lib = libName + (is64Bit ? "-64" : "");
		String libFileName = lib + (isLinux ? ".so" : ".dll");
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
				byte[] buffer = new byte[4096];
				while (true) {
					int length = input.read(buffer);
					if (length == -1)
						break;
					output.write(buffer, 0, length);
				}
				input.close();
				output.close();
			} catch (IOException ex) {
			} finally {
				try {
					if (input != null)
						input.close();
					if (output != null)
						output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.loadLibrary(lib);
	}
}
