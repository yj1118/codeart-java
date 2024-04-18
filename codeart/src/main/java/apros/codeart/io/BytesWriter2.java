//package apros.codeart.io;
//
//import java.nio.ByteBuffer;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.agrona.concurrent.UnsafeBuffer;
//import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
//import org.agrona.concurrent.ringbuffer.RingBuffer;
//import org.agrona.concurrent.ringbuffer.RingBufferDescriptor;
//
//public class BytesWriter2 {
//
//	private AtomicInteger _pointer = new AtomicInteger(-1);
//
//	private int next() {
//		return _pointer.updateAndGet(current -> (current + 1) % _segmentCount);
//	}
//
//	private final ManyToOneRingBuffer[] _segments;
//
//	private final int _segmentSize;
//
//	private final int _segmentCount;
//
//	/**
//	 * 
//	 * 缓冲区总大小
//	 * 
//	 * @return
//	 */
//	public int size() {
//		return _segmentSize * _segmentCount;
//	}
//
//	/**
//	 * @param segmentSize  每个分段的大小
//	 * @param segmentCount 分段数量
//	 */
//	private BytesWriter2(int segmentSize, int segmentCount) {
//		_segmentSize = segmentSize;
//		_segmentCount = segmentCount;
//		_segments = new ManyToOneRingBuffer[segmentCount];
//		allocateSegments();
//	}
//
//	/**
//	 * 构造时就创建分段，避免按需加载导致的并发控制，会增加额外的性能损耗
//	 */
//	private void allocateSegments() {
//		int BUFFER_SIZE = _segmentSize + RingBufferDescriptor.TRAILER_LENGTH;
//		for (var i = 0; i < _segments.length; i++) {
//			_segments[i] = new ManyToOneRingBuffer(new UnsafeBuffer(ByteBuffer.allocateDirect(BUFFER_SIZE)));
//		}
//	}
//
//	/**
//	 * 
//	 * 使用轮询的方式领取一个可用的分段
//	 * 
//	 * @return
//	 */
//	private RingBuffer claimSegment() {
//		var index = next(); // 取出下一个可用的分段坐标
//		return _segments[index];
//	}
//
//	public void using(int size) {
//
//		int i = 0;
//
//		while (i < _segmentCount) {
//			var rb = this.claimSegment();
//
//			int index = rb.tryClaim(1, size); // 尝试写入数据
//			if (index > 0) {
//
//				rb.commit(index);
//				return;
//			}
//
//			i++;
//		}
//
////		throw new  todo 这里要报错，已经找了所有的段都无法写入，证明数据都满了
//
//	}
//
//	/**
//	 * 
//	 * 创建以兆为单位的分段大小缓冲区
//	 * 
//	 * @param segmentSize  每个分段的大小（以MB为单位）
//	 * @param segmentCount 分段数量
//	 * @return
//	 */
//	public static BytesWriter2 createMB(int segmentSizeMB, int segmentCount) {
//		return new BytesWriter2(1024 * 1024 * segmentSizeMB, segmentCount);
//	}
//}
