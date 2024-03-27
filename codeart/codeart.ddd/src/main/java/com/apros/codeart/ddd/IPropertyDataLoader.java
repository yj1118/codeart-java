package com.apros.codeart.ddd;

/**
 * 属性数据加载器，可以自定义如何加载领域对象的属性的数据
 */
public interface IPropertyDataLoader {
	Object load(DynamicData data, QueryLevel level);
}
