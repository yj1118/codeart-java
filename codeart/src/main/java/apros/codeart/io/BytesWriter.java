package apros.codeart.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import apros.codeart.pooling.IPoolItem;

public final class BytesWriter implements AutoCloseable {

	private IPoolItem _poolItem;

	private ByteBuffer _buffer;

	public BytesWriter(int size) {
		_poolItem = IOBuffer.borrow(size);
		_buffer = _poolItem.getItem();
	}

	public void write(String value) {
		write(1); // 先写入4字节占位
		int start = _buffer.position();
		Charset charset = StandardCharsets.UTF_8; // 使用 UTF-8 编码
		CharsetEncoder encoder = charset.newEncoder();
		encoder.encode(CharBuffer.wrap(value), _buffer, true);
		int end = _buffer.position();
		int stringBytesLength = end - start;

		_buffer.position(start - 4); // 回到刚准备写字符串的位置
		write(stringBytesLength); // 写入字符串实际长度

		_buffer.position(end); // 回到字符串写完后的末尾位置
	}

	private void move(int length) {
		_buffer.position(_buffer.position() + length); // 保证读取后的偏移量

	}

	public void write(int value) {
		_buffer.putInt(value);
	}

	public void write(long value) {
		_buffer.putLong(value);
	}

	public void write(ByteBuffer src) {
		_buffer.put(src);
	}

	/**
	 * 
	 * 读取长度为 {@length} 的数组，以bufferr形式返回，这个方法的好处是建立了子视图，并没有真正的创建新的数组
	 * 
	 * @param length
	 * @return
	 */
	public ByteBuffer readBuffer(int length) {
		ByteBuffer slice = _buffer.slice(); // 不会复制字节数组
		slice.limit(length);
		move(length);
		return slice;
	}

	/**
	 * 是否还有数据
	 * 
	 * @return
	 */
	public boolean hasRemaining() {
		return _buffer.hasRemaining();
	}

	/**
	 * 
	 * 获得字节数，并且结束操作，意味着不要再使用writer做写操作了
	 * 
	 * 
	 * @return
	 */
	public byte[] toBytesAndEnd() {
		// buffer.flip() 是必须的步骤，因为它将限制设置为当前位置，然后将位置重置为零，从而使你能从缓冲区的开始读取数据。
		_buffer.flip(); // 切换模式，准备读取

		// 创建一个新数组，用于复制ByteBuffer中的数据
		/*
		 * remaining() 方法用来获取当前位置（position）和限制（limit）之间的元素数量。
		 * 这个方法非常重要，因为它告诉你还有多少元素可以从缓冲区中读取或向缓冲区中写入。
		 * 
		 * public final int remaining() { return limit - position; }
		 * 
		 */
		byte[] bytes = new byte[_buffer.remaining()];
		_buffer.get(bytes); // 将数据从ByteBuffer复制到byte数组
		return bytes;
	}

	public byte[] toBytes() {
		var position = _buffer.position();
		var bytes = toBytesAndEnd();

		// 重置状态到最后一次写的地方
		_buffer.clear(); // 注意这个方法并不是清空数据
		_buffer.position(position);

		return bytes;
	}

	@Override
	public void close() {
		_poolItem.close();
	}

}
