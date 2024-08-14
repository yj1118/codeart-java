package apros.codeart.dto.serialization.internal;

import java.time.Instant;

import apros.codeart.dto.DTObject;
import apros.codeart.dto.IDTOReader;

abstract class DTOReader implements IDTOReader {

    protected DTObject _dto;

    public DTOReader(DTObject dto) {
        _dto = dto;
    }

    public boolean exist(String name) {
        return _dto.exist(name);
    }

    public boolean readBoolean(String name) {
        return _dto.getBoolean(name);
    }

    public byte readByte(String name) {
        return _dto.getByte(name);
    }

    public char readChar(String name) {
        return _dto.getChar(name);
    }

    public float readFloat(String name) {
        return _dto.getFloat(name);
    }

    public double readDouble(String name) {
        return _dto.getDouble(name);
    }

    public short readShort(String name) {
        return _dto.getShort(name);
    }

    public int readInt(String name) {
        return _dto.getInt(name);
    }

    public long readLong(String name) {
        return _dto.getLong(name);
    }

    public String readString(String name) {
        return _dto.getString(name);
    }

    public Instant readInstant(String name) {
        return _dto.getInstant(name);
    }

    public Object readValue(String name) {
        return _dto.getValue(name);
    }

    /**
     * 读取数组长度
     */
    public int readLength(String name) {
        var dtoList = _dto.getList(name, false);
        if (dtoList == null)
            return -1; //-1表示没有数据
        return dtoList.size();
    }

//	public abstract T ReadElement<T>(
//	string name,
//	int index);

    public abstract Object readObject(Class<?> objectType, String name);

    private DTObject getElement(String name, int index) {
        return _dto.getElement(name, index, true);
    }


    @Override
    public boolean readElementBoolean(String name, int index) {
        var e = getElement(name, index);
        return e.getBoolean();
    }


    @Override
    public byte readElementByte(String name, int index) {
        var e = getElement(name, index);
        return e.getByte();
    }

    @Override
    public short readElementShort(String name, int index) {
        var e = getElement(name, index);
        return e.getShort();
    }

    @Override
    public int readElementInt(String name, int index) {
        var e = getElement(name, index);
        return e.getInt();
    }

    @Override
    public long readElementLong(String name, int index) {
        var e = getElement(name, index);
        return e.getLong();
    }

    @Override
    public float readElementFloat(String name, int index) {
        var e = getElement(name, index);
        return e.getFloat();
    }

    @Override
    public double readElementDouble(String name, int index) {
        var e = getElement(name, index);
        return e.getDouble();
    }

    @Override
    public char readElementChar(String name, int index) {
        var e = getElement(name, index);
        return e.getChar();
    }

}
