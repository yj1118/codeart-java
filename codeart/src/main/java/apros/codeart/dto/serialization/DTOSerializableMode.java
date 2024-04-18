package apros.codeart.dto.serialization;

public enum DTOSerializableMode {

	/**
	 * 常规
	 */
	General,

	/**
	 * 函数模式,该模式表示对象有一部分属性用于接受DTO的值，另外一部分属性用于传递值到DTO中
	 */
	Function
}
