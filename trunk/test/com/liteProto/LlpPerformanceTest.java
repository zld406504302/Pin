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
		//llpJava.delMessage("test.mes.lpb");
		LlpMessage mes = llpJava.getMessage("testLlpDataType.test");
		LlpMessage mes2 = llpJava.getMessage("testLlpDataType");
		
		long t1, t2;
		t1 = System.currentTimeMillis();
		System.out.println("start time= " + t1 + "\n");
		for(int i = 0; i< 1000000; i++) {
			mes.write("i32", 1234);
			mes.write("i64", (long)568);
			mes.write("f32", (float)5678.0);
			mes.write("f64", (double)58.2);
			mes.write("str", "hi, I am string!");
			
			mes.write("al", 123);
			mes.write("al", 123);
			mes.write("al", 123);
			
			int lens = mes.readSize("al");
			for(int j=0; j<lens; j++)
				mes.readInt("al");
			
			mes2.decode(mes.encode());
			mes.clear();
		}

		t2 = System.currentTimeMillis();
		System.out.println("end time= " + t2);
		System.out.println("all time= " + (t2 - t1));
		
		System.out.println( "i32 = "+mes2.readInt("i32")+
							"\ni64 = "+mes2.readLong("i64")+
							"\nf32 = "+mes2.readFloat("f32")+
							"\nf64 = "+mes2.readDouble("f64")+
							"\nstr = "+mes2.readString("str"));

		mes2.destory();
		mes.destory();
		llpJava.destory();
	}

}
