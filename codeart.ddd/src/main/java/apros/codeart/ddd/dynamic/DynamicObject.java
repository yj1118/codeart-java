package apros.codeart.ddd.dynamic;

import apros.codeart.ddd.DDDConfig;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.FrameworkDomain;
import apros.codeart.ddd.MergeDomain;
import apros.codeart.ddd.repository.ConstructorRepository;
import apros.codeart.dto.DTObject;

@MergeDomain
@FrameworkDomain
public class DynamicObject extends DomainObject implements IDynamicObject {

	private boolean _isEmpty;

	public boolean isEmpty() {
		return _isEmpty;
	}

	public DynamicObject(boolean isEmpty) {
		_isEmpty = isEmpty;
		this.onConstructed();
	}

	@ConstructorRepository
	public DynamicObject() {
		_isEmpty = false;
		this.onConstructed();
	}

//	/**
//	 * 将对象 {@target} 的数据同步到当前对象中
//	 * 
//	 * @param target
//	 */
//	public void sync(DynamicObject target) {
//		var meta = ObjectMetaLoader.get(this.getClass());
//		for (var property : meta.properties()) {
//			var value = target.getValue(property.name());
//			this.loadValue(property.name(), value);
//		}
//	}
//
//	/**
//	 * 得到动态对象内部所有的动态根类型的成员对象（不包括当前对象自身，也不包括空的根对象）
//	 */
//	public Iterable<DynamicRoot> getRefRoots() {
//		ArrayList<DynamicRoot> roots = new ArrayList<>();
//		ArrayList<Object> processed = new ArrayList<>(); // 防止死循环的额外参数
//
//		this.fillRefRoots(roots, processed, DynamicRoot.class);
//
//		return roots;
//	}
//
//	/**
//	 * 为了防止死循环，增加了该方法
//	 * 
//	 * @param roots 将当前对象直接或间接引用到的根对象，填充到 roots里
//	 */
//	protected <T extends DynamicRoot> void fillRefRoots(ArrayList<T> roots, ArrayList<Object> processed,
//			Class<T> rootType) {
//		var meta = ObjectMetaLoader.get(this.getClass());
//		for (var property : meta.properties()) {
//			switch (property.category()) {
//			case DomainPropertyCategory.AggregateRoot: {
//				var value = this.getValue(property.name());
//				if (processed.contains(value))
//					continue;
//				processed.add(value);
//
//				var root = TypeUtil.as(value, rootType);
//				if (root != null && !root.isEmpty()) {
//					roots.add(root);
//					root.fillRefRoots(roots, processed, rootType);
//				}
//			}
//				break;
//			case DomainPropertyCategory.EntityObject:
//			case DomainPropertyCategory.ValueObject: {
//				var value = this.getValue(property.name());
//				if (processed.contains(value))
//					continue;
//				processed.add(value);
//
//				var obj = TypeUtil.as(value, DynamicObject.class);
//				if (obj != null && !obj.isEmpty()) {
//					obj.fillRefRoots(roots, processed, rootType);
//				}
//			}
//				break;
//			case DomainPropertyCategory.AggregateRootList: {
//				var value = this.getValue(property.name());
//				if (processed.contains(value))
//					continue;
//				processed.add(value);
//
//				var list = TypeUtil.as(value, Iterable.class);
//				for (var obj : list) {
//					if (processed.contains(obj))
//						continue;
//					processed.add(obj);
//
//					var root = TypeUtil.as(obj, rootType);
//					if (root != null && !root.isEmpty()) {
//						roots.add(root);
//						root.fillRefRoots(roots, processed, rootType);
//					}
//				}
//			}
//				break;
//			case DomainPropertyCategory.EntityObjectList:
//			case DomainPropertyCategory.ValueObjectList: {
//				var value = this.getValue(property.name());
//				if (processed.contains(value))
//					continue;
//				processed.add(value);
//
//				var list = TypeUtil.as(value, Iterable.class);
//				for (var obj : list) {
//					if (processed.contains(obj))
//						continue;
//					processed.add(obj);
//
//					var o = TypeUtil.as(obj, DynamicObject.class);
//					if (o != null && !o.isEmpty()) {
//						o.fillRefRoots(roots, processed, rootType);
//					}
//				}
//
//			}
//				break;
//			default:
//				break;
//			}
//		}
//	}

	/**
	 * 
	 * 获取通过配置文件得到的对象元数据
	 * 
	 * @param objectName
	 * @return
	 */
	public static DTObject getMetadata(String objectName) {
		var meta = DDDConfig.objectMeta();
		return meta.getObject(objectName, null);
	}

}
