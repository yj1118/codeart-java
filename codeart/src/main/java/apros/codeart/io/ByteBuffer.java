package apros.codeart.io;

import java.util.concurrent.atomic.AtomicInteger;

import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;
import org.agrona.concurrent.ringbuffer.RingBufferDescriptor;

public class ByteBuffer {

	private AtomicInteger _pointer = new AtomicInteger(-1);

	private int next() {
		return _pointer.updateAndGet(current -> (current + 1) % _segmentCount);
	}

	private final ManyToOneRingBuffer[] _segments;

	private final int _segmentSize;

	private final int _segmentCount;

	/**
	 * 
	 * 缓冲区总大小
	 * 
	 * @return
	 */
	public int size() {
		return _segmentSize * _segmentCount;
	}

	/**
	 * @param segmentSize  每个分段的大小
	 * @param segmentCount 分段数量
	 */
	private ByteBuffer(int segmentSize, int segmentCount) {
		_segmentSize = segmentSize;
		_segmentCount = segmentCount;
		_segments = new ManyToOneRingBuffer[segmentCount];
		allocateSegments();
	}

	/**
	 * 构造时就创建分段，避免按需加载导致的并发控制，会增加额外的性能损耗
	 */
	private void allocateSegments() {
		int BUFFER_SIZE = _segmentSize + RingBufferDescriptor.TRAILER_LENGTH;
		for (var i = 0; i < _segments.length; i++) {
			_segments[i] = new ManyToOneRingBuffer(new UnsafeBuffer(java.nio.ByteBuffer.allocateDirect(BUFFER_SIZE)));
		}
	}

	/**
	 * 
	 * 使用轮询的方式领取一个可用的分段
	 * 
	 * @return
	 */
	private RingBuffer claimSegment() {
		var index = next(); // 取出下一个可用的分段坐标
		return _segments[index];
	}

	/**
	 * 
	 * 创建一个分段大小为1兆，拥有{@segmentCount}个分段的字节缓冲区
	 * 
	 * @param segmentCount
	 * @return
	 */
	public static ByteBuffer createMB(int segmentCount) {
		return createMB(1, segmentCount);
	}

	/**
	 * 
	 * 创建以兆为单位的分段大小缓冲区
	 * 
	 * @param segmentSize  每个分段的大小（以MB为单位）
	 * @param segmentCount 分段数量
	 * @return
	 */
	public static ByteBuffer createMB(int segmentSizeMB, int segmentCount) {
		return new ByteBuffer(1024 * 1024 * segmentSizeMB, segmentCount);
	}

	/**
	 * 
	 * 创建以KB为单位的分段大小缓冲区
	 * 
	 * @param segmentSize  每个分段的大小（以KB为单位）
	 * @param segmentCount 分段数量
	 * @return
	 */
	public static ByteBuffer createKB(int segmentSizeKB, int segmentCount) {
		return new ByteBuffer(1024 * segmentSizeKB, segmentCount);
	}

}
