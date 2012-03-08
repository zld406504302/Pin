package pin.performance;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.DirectChannelBufferFactory;
import org.jboss.netty.buffer.HeapChannelBufferFactory;

public class ChannelBufferAllocatePerformanceTest {

	public static void main(String[] args) throws Exception {
		System.out.println("DirectChannelBuffer allocate test...");
		long t1, t2;
		t1 = System.currentTimeMillis();

		for (int i = 0; i < 1000000; i++) {
			ChannelBuffer bufferDirect = DirectChannelBufferFactory
					.getInstance().getBuffer(5000);
			bufferDirect.writeByte(1);
			bufferDirect.writeShort(2);
			bufferDirect.writeInt(3);
			bufferDirect.writeLong(4);
			bufferDirect.writeFloat(5.0f);
			bufferDirect.writeDouble(6.0d);
			bufferDirect.clear();
		}
		t2 = System.currentTimeMillis();

		System.out.println("DirectChannelBuffer allocate cost time :"
				+ (t2 - t1));

		System.out.println("HeapChannelBuffer allocate test...");
		t1 = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			ChannelBuffer bufferHeap = HeapChannelBufferFactory.getInstance()
					.getBuffer(5000);
			bufferHeap.writeByte(1);
			bufferHeap.writeShort(2);
			bufferHeap.writeInt(3);
			bufferHeap.writeLong(4);
			bufferHeap.writeFloat(5.0f);
			bufferHeap.writeDouble(6.0d);
			bufferHeap.clear();
		}

		t2 = System.currentTimeMillis();

		System.out
				.println("HeapChannelBuffer allocate cost time :" + (t2 - t1));

	}
}
