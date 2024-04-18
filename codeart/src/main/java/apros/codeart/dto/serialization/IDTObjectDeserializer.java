package apros.codeart.dto.serialization;

import apros.codeart.dto.DTObject;

public interface IDTObjectDeserializer {

	/**
	 * 将dto的信息反序列化到对象中
	 * 
	 * @param objectType
	 * @param dto
	 * @return
	 */
	Object deserialize(Class<?> objectType, DTObject dto);

}
