package com.liteProto;

import com.liteProto.LlpJava;
import com.liteProto.LlpMessage;

public class LlpPerformanceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		LlpJava llpJava = LlpJava.instance();

		llpJava.regMessage("./lpb/testLlp.mes.lpb");
		// llpJava.delMessage("test.mes.lpb");
		LlpMessage mes = llpJava.getMessage("testLlpDataType");
		LlpMessage mes2 = llpJava.getMessage("testLlpDataType");

		long t1, t2;
		byte[] data = new byte[] { 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
				3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
				3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 };
		t1 = System.currentTimeMillis();
		System.out.println("start time= " + t1 + "\n");
		for (int i = 0; i < 1000000; i++) {
			mes.write("i32", 1234);
			mes.write("i64", 568l);
			mes.write("f32", 5678.0f);
			mes.write("f64", 58.2d);
			mes.write("data", data);
			mes.write("str", "hi, I am string!");
			mes2.decode(mes.encode());
			mes.clear();
		}

		t2 = System.currentTimeMillis();
		System.out.println("end time= " + t2);
		System.out.println("all time= " + (t2 - t1));
		/*
		 * System.out.println( "i32 = "+mes2.readInt("i32")+
		 * "\ni64 = "+mes2.readLong("i64")+ "\nf32 = "+mes2.readFloat("f32")+
		 * "\nf64 = "+mes2.readDouble("f64")+
		 * "\nstr = "+mes2.readString("str"));
		 */
		mes2.destory();
		mes.destory();
		llpJava.destory();
		System.gc();
		System.in.read();
	}

}
