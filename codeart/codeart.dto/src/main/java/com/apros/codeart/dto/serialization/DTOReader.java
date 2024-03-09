package com.apros.codeart.dto.serialization;

import java.time.Instant;

import com.apros.codeart.dto.DTObject;
import com.apros.codeart.dto.IDTOReader;
import com.apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

abstract class DTOReader implements IDTOReader {

	protected DTObject _dto;

	public DTOReader(DTObject dto) {
		_dto = dto;
	}

	public boolean readBoolean(String name) {
		return _dto.getBoolean(name, false);
	}

	public byte readByte(String name) {
		return _dto.getByte(name, (byte) 0);
	}

	public char readChar(String name) {
		return _dto.getChar(name, StringUtil.charEmpty());
	}

	public float readFloat(String name) {
		return _dto.getFloat(name, (float) 0);
	}

	public double readDouble(String name) {
		return _dto.getDouble(name, (double) 0);
	}

	public short readShort(String name) {
		return _dto.getShort(name, (short) 0);
	}

	public int readInt(String name) {
		return _dto.getInt(name, 0);
	}

	public long readLong(String name) {
		return _dto.getLong(name, 0L);
	}

	public String readString(String name) {
		return _dto.getString(name, StringUtil.empty());
	}

	public Instant readInstant(String name) {
		return _dto.getInstant(name);
	}

	/**
	 * 读取数组长度
	 */
	public int readLength(String name) {
		var dtoList = _dto.getList(name, false);
		if (dtoList == null)
			return 0;
		return Iterables.size(dtoList);
	}

//	public abstract T ReadElement<T>(
//	string name,
//	int index);

	public abstract Object readObject(Class<?> objectType, String name);
}
