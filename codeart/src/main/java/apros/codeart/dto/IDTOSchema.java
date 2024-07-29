package apros.codeart.dto;

public interface IDTOSchema {

    /**
     * 匹配架构名称，返回匹配到的名称（由于大小写区别，所以需要得到实际名称）
     *
     * @param name
     * @return
     */
    String matchChildSchema(String name);

    /**
     * 获取子架构
     *
     * @param name
     * @return
     */
    IDTOSchema getChildSchema(String name);
}
