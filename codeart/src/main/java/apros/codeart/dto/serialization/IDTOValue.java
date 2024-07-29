package apros.codeart.dto.serialization;

import apros.codeart.dto.DTObject;

public interface IDTOValue {

    // 指示对象可以作为dto的value，如何输出为json格式的字符串
    void writeValue(StringBuilder code);
}
