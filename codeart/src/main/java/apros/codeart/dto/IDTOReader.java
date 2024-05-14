package apros.codeart.dto;

import java.time.Instant;

public interface IDTOReader {

	boolean exist(String name);

	boolean readBoolean(String name);

	/**
	 * 从流中读取一个字节
	 * 
	 * @param name
	 * @return
	 */
	byte readByte(String name);

	short readShort(String name);

	int readInt(String name);

	long readLong(String name);

	float readFloat(String name);

	double readDouble(String name);

	/// <summary>
	/// 从流中读取一个非空的字符
	/// </summary>
	/// <returns></returns>
	char readChar(String name);

	Instant readInstant(String name);

	String readString(String name);

	/**
	 * 读取数组长度
	 * 
	 * @param name
	 * @return
	 */
	int readLength(String name);

	Object readElement(String name, int index, Class<?> elementType);

	Object readObject(Class<?> objectType, String name);
}
