package apros.codeart.dto.serialization;

import apros.codeart.dto.DTObject;

public interface IDTOSerializable {

    /**
     * 将自身的内容序列化到 owner 中
     *
     * @param owner
     * @param name
     */
    void serialize(DTObject owner, String name);

    /**
     * 获得对象的dto形式
     *
     * @return
     */
    DTObject getData(String schemaCode);
}
