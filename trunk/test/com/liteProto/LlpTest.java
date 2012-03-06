package com.liteProto;

import com.liteProto.LlpJava;
import com.liteProto.LlpMessage;

public class LlpTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		LlpJava llpJava = LlpJava.instance();

		llpJava.regMessage("testLlp.mes.lpb");
		//llpJava.delMessage("test.mes.lpb");
		LlpMessage mes = llpJava.getMessage("at");
		LlpMessage mes2 = llpJava.getMessage("at");
		long t1, t2;
		t1 = System.currentTimeMillis();
		System.out.println("start time= " + t1 + "\n");
		for (int i = 0; i < 1000000; i++) {
			mes.write("aa", 1234);
			mes.write("bb", 5678);
			mes.write("cc", "hi, I am string!");
			mes2.decode(mes.encode());

			mes.clear();
		}
		t2 = System.currentTimeMillis();
		System.out.println("end time= " + t2);
		System.out.println("all time= " + (t2 - t1));

		System.out.println("aa = " + mes2.readInt("aa") + "\nbb= "
				+ mes2.readInt("bb") + "\ncc= " + mes2.readString("cc"));

		mes2.destory();
		mes.destory();
		llpJava.destory();
	}

}
