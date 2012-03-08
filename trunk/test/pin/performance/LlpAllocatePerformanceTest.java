package pin.performance;

import com.liteProto.LlpJava;
import com.liteProto.LlpMessage;

public class LlpAllocatePerformanceTest {
	public static void main(String[] args) throws Exception {
		LlpJava llpJava = LlpJava.instance();

		llpJava.regMessage("./lpb/testLlp.mes.lpb");

		long t1, t2;
		byte[] data = new byte[] { 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
				3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
				3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 };
		t1 = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			LlpMessage mes = llpJava.getMessage("testLlpDataType");
			mes.write("i32", 1234);
			mes.write("i64", 568l);
			mes.write("f32", 5678.0f);
			mes.write("f64", 58.2d);
			mes.write("data", data);
			mes.write("str", "hi, I am string!");
			mes.decode(mes.encode());
			mes.destory();
		}
		t2 = System.currentTimeMillis();
		System.out.println("test1 all time= " + (t2 - t1));

		t1 = System.currentTimeMillis();
		LlpMessage mes2 = llpJava.getMessage("testLlpDataType");
		for (int i = 0; i < 1000000; i++) {
			mes2.write("i32", 1234);
			mes2.write("i64", 568l);
			mes2.write("f32", 5678.0f);
			mes2.write("f64", 58.2d);
			mes2.write("data", data);
			mes2.write("str", "hi, I am string!");
			mes2.decode(mes2.encode());
			mes2.clear();
		}
		mes2.destory();

		t2 = System.currentTimeMillis();
		System.out.println("test2 all time= " + (t2 - t1));

		llpJava.destory();
	}
}
