package pin.main;


public class Pin {

	public static final boolean isLinux = System.getProperty("os.name").contains("Linux");
	public static final boolean is64Bit = System.getProperty("os.arch").equals("amd64");
	
	public static void loadLibrary(String libName) {
		if(is64Bit) {
			System.loadLibrary(libName + "-64");
		} else {
			System.loadLibrary(libName);
		}
	}

}
