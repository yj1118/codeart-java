package com.apros.codeart.dto.serialization;

import java.time.LocalDateTime;

import com.apros.codeart.dto.DTObject;

abstract class DTOWriter implements IDTOWriter {

	protected DTObject _dto;

	public DTOWriter() {
	}

	public void write(String name, byte value) {
		_dto.setByte(name, value);
	}

	public void write(String name, short value) {
		_dto.setShort(name, value);
	}

	public void write(String name, int value) {
		_dto.setInt(name, value);
	}

	public void write(String name, long value) {
		_dto.setLong(name, value);
	}

	public void write(String name, float value) {
		_dto.setFloat(name, value);
	}

	public void write(String name, double value) {
		_dto.setDouble(name, value);
	}

	public void write(String name, LocalDateTime value) {
		_dto.setLocalDateTime(name, value);
	}

	public void write(String name, String value) {
		_dto.setString(name, value);
	}

	public void write(String name, boolean value) {
		_dto.setBoolean(name, value);
	}

	public void write(String name, char value) {
		_dto.setChar(name, value);
	}

//	public abstract void WriteElement<T>(
//	string name, bool elementIsPrimitive,
//	T element);

	public abstract void write(String name, Object value);

	public void writeElement(String name, Object telement) {

	}

	public void writeArray(String name) {
		_dto.setList(name);
	}
}
