package apros.codeart.dto;

import java.util.ArrayList;
import java.util.function.Function;

import apros.codeart.context.ContextSession;
import apros.codeart.pooling.IReusable;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.pooling.PoolItemPhase;
import apros.codeart.util.Action1;
import apros.codeart.util.Func1;

abstract class DTEntity implements IReusable {

	private String _name;

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	private DTEntity _parent;

	public DTEntity getParent() {
		return _parent;
	}

	public void setParent(DTEntity parent) {
		_parent = parent;
	}

	public DTEntity() {
	}

	/**
	 * dto成员是否含有数据
	 * 
	 * @return
	 */
	public abstract boolean hasData();

	public abstract DTEntityType getType();

	private Iterable<DTEntity> _selfAsEntities;

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Iterable<DTEntity> getSelfAsEntities() throws Exception {
		if (_selfAsEntities == null) {
			var es = obtainList();
			es.add(this);
			_selfAsEntities = es;
		}
		return _selfAsEntities;
	}

	public final Object clone() throws CloneNotSupportedException {
		try {
			return cloneImpl();
		} catch (Exception e) {
			throw new CloneNotSupportedException(e.getMessage());
		}
	}

	protected abstract DTEntity cloneImpl() throws Exception;

	public abstract void setMember(QueryExpression query, Function<String, DTEntity> createEntity) throws Exception;

	/**
	 * 删除成员
	 * 
	 * @param e
	 */
	public abstract void removeMember(DTEntity e);

	/**
	 * 根据查找表达式找出dto成员
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public abstract Iterable<DTEntity> finds(QueryExpression query) throws Exception;

	/**
	 * @param sequential 是否排序输出代码
	 * @param outputName 是否输出key值
	 * @return
	 * @throws Exception
	 */
	public abstract String getCode(boolean sequential, boolean outputName) throws Exception;

	/**
	 * 输出架构码
	 * 
	 * @param sequential
	 * @param outputName
	 * @return
	 */
	public abstract String getSchemaCode(boolean sequential, boolean outputName) throws Exception;

	public void clear() throws Exception {
		_name = null;
		_parent = null;
		_selfAsEntities = null;
	}

	public abstract void clearData();

	private static Pool<ArrayList<DTEntity>> listPool = new Pool<ArrayList<DTEntity>>(() -> {
		return new ArrayList<DTEntity>();
	}, (list, phase) -> {
		if (phase == PoolItemPhase.Returning) {
			list.clear(); // 集合清空，集合的成员也是由池中借出来的，自然会被回收，所以这里不用再每个项清理
		}
		return true;
	}, PoolConfig.onlyMaxRemainTime(300));

	public static ArrayList<DTEntity> obtainList() {
		return ContextSession.obtainItem(listPool, () -> new ArrayList<DTEntity>());
	}

	public static void usingList(Action1<ArrayList<DTEntity>> action) throws Exception {
		listPool.using(action);
	}

	public static <R> R usingList(Func1<ArrayList<DTEntity>, R> action) throws Exception {
		return listPool.using(action);
	}

}
