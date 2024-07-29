package apros.codeart.io;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class BytesReader {

    private final ByteBuffer _buffer;

    public BytesReader(byte[] content) {
        _buffer = ByteBuffer.wrap(content);
    }

    public String readString() {
        var length = readInt(); // 数组长度
        ByteBuffer slice = _buffer.slice(); // 不会复制字节数组
        slice.limit(length);
        var result = StandardCharsets.UTF_8.decode(slice).toString();
        move(length);
        return result;
    }

    private void move(int length) {
        _buffer.position(_buffer.position() + length); // 保证读取后的偏移量

    }

    public int readInt() {
        return _buffer.getInt();
    }

    public long readLong() {
        return _buffer.getLong();
    }

    /**
     * 读取长度为 {@length} 的数组，以buferr形式返回，这个方法的好处是建立了子视图，并没有真正的创建新的数组
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
