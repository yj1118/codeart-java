package apros.codeart.dto.serialization.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import apros.codeart.dto.DTObject;
import apros.codeart.dto.IDTOWriter;

abstract class DTOWriter implements IDTOWriter {

    protected DTObject _dto;

    public DTOWriter(DTObject dto) {
        _dto = dto;
    }

    @Override
    public void writeByte(String name, byte value) {
        _dto.setByte(name, value);
    }

    @Override
    public void writeShort(String name, short value) {
        _dto.setShort(name, value);
    }

    @Override
    public void writeInt(String name, int value) {
        _dto.setInt(name, value);
    }

    @Override
    public void writeLong(String name, long value) {
        _dto.setLong(name, value);
    }

    @Override
    public void writeFloat(String name, float value) {
        _dto.setFloat(name, value);
    }

    @Override
    public void writeDouble(String name, double value) {
        _dto.setDouble(name, value);
    }

    @Override
    public void writeLocalDateTime(String name, LocalDateTime value) {
        _dto.setLocalDateTime(name, value);
    }

    @Override
    public void writeInstant(String name, Instant value) {
        _dto.setInstant(name, value);
    }

    @Override
    public void writeString(String name, String value) {
        _dto.setString(name, value);
    }

    @Override
    public void writeBoolean(String name, boolean value) {
        _dto.setBoolean(name, value);
    }

    @Override
    public void writeChar(String name, char value) {
        _dto.setChar(name, value);
    }

    @Override
    public void writeValue(String name, Object value) {
        _dto.setValue(name, value);
    }

    @Override
    public void writeBigDecimal(String name, BigDecimal value) {
        _dto.setValue(name, value);
    }


//	public abstract void WriteElement<T>(
//	string name, bool elementIsPrimitive,
//	T element);

    public abstract void writeObject(String name, Object value);

    @Override
    public void writeElement(String name, Object element) {
        var e = _dto.isReadOnly() ? DTObject.readonly(element) : DTObject.editable(element);
        _dto.push(name, e);
    }

    @Override
    public void writeArray(String name) {
        _dto.obtainList(name);
    }

    @Override
    public void writeElementByte(String name, byte value) {
        _dto.pushByte(name, value);
    }

    @Override
    public void writeElementShort(String name, short value) {
        _dto.pushShort(name, value);
    }

    @Override
    public void writeElementInt(String name, int value) {
        _dto.pushInt(name, value);
    }

    @Override
    public void writeElementLong(String name, long value) {
        _dto.pushLong(name, value);
    }

    @Override
    public void writeElementFloat(String name, float value) {
        _dto.pushFloat(name, value);
    }

    @Override
    public void writeElementDouble(String name, double value) {
        _dto.pushDouble(name, value);
    }

    @Override
    public void writeElementChar(String name, char value) {
        _dto.pushChar(name, value);
    }

    @Override
    public void writeElementBoolean(String name, boolean value) {
        _dto.pushBoolean(name, value);
    }

}
