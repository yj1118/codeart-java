package apros.codeart.ddd;

import java.lang.Class;
import java.util.function.BiFunction;

public interface IDomainProperty {

	/**
	 * 领域属性的名称
	 */
	String getName();
	
	
    /**
     * 得到领域属性对应的值的类型信息
     * @return
     */
    Class<?> getValueClass();
    
    
    /**
     * 领域属性所属的领域对象的类型信息
     * @return
     */
    Class<?> getOwnerClass();
    
    
    /// <summary>
    /// 获得属性的默认值,传入属性所在的对象和成员对应的属性定义
    /// </summary>
    /// <returns></returns>
    BiFunction<IDomainObject, IDomainProperty, Object> makeGetDefaultValue();
    
   
}