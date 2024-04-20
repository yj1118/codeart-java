package apros.codeart.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public final class BytesWriter {

	private ByteBuffer _buffer;

	public BytesWriter(int size) {
		_buffer = ByteBuffer.allocate(size);
	}

	public void write(String value) {
		writeInt(1); // 先写入4字节占位
		int start = _buffer.position();
		Charset charset = StandardCharsets.UTF_8; // 使用 UTF-8 编码
		CharsetEncoder encoder = charset.newEncoder();
		encoder.encode(CharBuffer.wrap(value), _buffer, true);
		int end = _buffer.position();
		int stringBytesLength = end - start;

		_buffer.position(start - 4); // 回到刚准备写字符串的位置
		writeInt(stringBytesLength); // 写入字符串实际长度

		_buffer.position(end); // 回到字符串写完后的末尾位置
	}

	private void move(int length) {
		_buffer.position(_buffer.position() + length); // 保证读取后的偏移量

	}

	public void writeInt(int value) {
		_buffer.putInt(value);
	}

	public void writeLong(long value) {
		_buffer.putLong(value);
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

}
