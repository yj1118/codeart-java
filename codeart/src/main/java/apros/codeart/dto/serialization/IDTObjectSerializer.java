package apros.codeart.dto.serialization;

import apros.codeart.dto.DTObject;

public interface IDTObjectSerializer {

	/**
	 * 将对象实例的信息序列化到dto中
	 * 
	 * @param instance
	 * @return
	 */
	DTObject serialize(Object instance);
}
