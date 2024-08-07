package apros.codeart.dto.serialization.internal;

import java.time.Instant;
import java.time.LocalDateTime;

import apros.codeart.dto.DTObject;
import apros.codeart.dto.IDTOWriter;

abstract class DTOWriter implements IDTOWriter {

    protected DTObject _dto;

    public DTOWriter(DTObject dto) {
        _dto = dto;
    }

    public void writeByte(String name, byte value) {
        _dto.setByte(name, value);
    }

    public void writeShort(String name, short value) {
        _dto.setShort(name, value);
    }

    public void writeInt(String name, int value) {
        _dto.setInt(name, value);
    }

    public void writeLong(String name, long value) {
        _dto.setLong(name, value);
    }

    public void writeFloat(String name, float value) {
        _dto.setFloat(name, value);
    }

    public void writeDouble(String name, double value) {
        _dto.setDouble(name, value);
    }

    public void writeLocalDateTime(String name, LocalDateTime value) {
        _dto.setLocalDateTime(name, value);
    }

    public void writeInstant(String name, Instant value) {
        _dto.setInstant(name, value);
    }

    public void writeString(String name, String value) {
        _dto.setString(name, value);
    }

    public void writeBoolean(String name, boolean value) {
        _dto.setBoolean(name, value);
    }

    public void writeChar(String name, char value) {
        _dto.setChar(name, value);
    }

    public void writeValue(String name, Object value) {
        _dto.setValue(name, value);
    }


//	public abstract void WriteElement<T>(
//	string name, bool elementIsPrimitive,
//	T element);

    public abstract void writeObject(String name, Object value);

    public void writeElement(String name, Object element) {
        var e = _dto.isReadOnly() ? DTObject.readonly(element) : DTObject.editable(element);
        _dto.push(name, e);
    }

    public void writeArray(String name) {
        _dto.obtainList(name);
    }
}
