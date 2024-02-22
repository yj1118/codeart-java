package apros.codeart.dto;

import static apros.codeart.runtime.Util.as;
import static apros.codeart.util.StringUtil.isNullOrEmpty;

import java.util.ArrayList;
import java.util.function.Function;

import apros.codeart.context.ContextSession;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.pooling.util.StringPool;
import apros.codeart.util.Func1;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

final class DTEObject extends DTEntity {

	private ArrayList<DTEntity> _members;

	private void setMembers(ArrayList<DTEntity> membres) {
		_members = membres;
		for (var e : _members) {
			e.setParent(this);
		}
	}

	private DTEObject() {
	}

	public ArrayList<DTEntity> GetMembers() {
		return _members;
	}

	public DTEntity first() {
		return ListUtil.first(_members);
	}

	@Override
	public DTEntity cloneImpl() throws Exception {
		var items = DTEntity.obtainList();
		for (var e : _members) {
			items.add((DTEntity) e.clone());
		}

		var dte = obtain(items);
		dte.setName(this.getName());
		return dte;
	}

	@Override
	public void clear() throws Exception {
		super.clear();
		// 这里只用断开与数组的连接，不用清理members
		// 因为member是从池里取的，当上下文会话结束，会自动回收
		_members = null;
	}

	@Override
	public void clearData() {
		if (_members != null) {
			for (var member : _members)
				member.clearData();
		}
	}

	/**
	 * 是否存在成员
	 * 
	 * @param memberName
	 * @return
	 */
	public boolean exist(String memberName) {
		return ListUtil.find(_members, (t) -> t.getName().equalsIgnoreCase(memberName)) != null;
	}

	public boolean remove(String memberName) {
		return ListUtil.remove(_members, (t) -> t.getName().equalsIgnoreCase(memberName)) != null;
	}

	public DTEntity find(String memberName) {
		return ListUtil.find(_members, (t) -> t.getName().equalsIgnoreCase(memberName));
	}

	@Override
	public boolean hasData() {
		for (var member : _members)
			if (member.hasData())
				return true;
		return false;
	}

	@Override
	public DTEntityType getType() {
		return DTEntityType.OBJECT;
	}

	@Override
	public Iterable<DTEntity> finds(QueryExpression query) throws Exception {
		if (query.onlySelf())
			return this.getSelfAsEntities();// 查询自身

		var segment = query.getSegment();

		var entity = this.find(segment);

		if (entity == null) {
			if (!query.hasNext() && _members.size() == 1 && isNullOrEmpty(ListUtil.first(_members).getName())) {
				// 例如:{[{id,name}]} id=>id2 , {{name,sex}} name=>name2
				return ListUtil.first(_members).finds(query);
			} else {
//		        if (query.isEmpty()) return _entities.ToArray(); //原始代码拷贝了一份，考虑到性能，我们这里不拷贝，如果引起BUG，我们再考虑是否优化调用过程
				// 原则上，查询的结果是不能被修改的
				if (query.isEmpty())
					return _members;
			}
		} else {
			if (!query.hasNext()) {
				// 没有后续查找
				return entity.getSelfAsEntities();
			} else {
				return entity.finds(query.getNext());
			}
		}
		return DTEntity.obtainList();
	}

	/**
	 * 设置成员，当成员存在时覆盖，成员不存在时新增
	 * 
	 * @throws Exception
	 */
	@Override
	public void setMember(QueryExpression query, Function<String, DTEntity> createEntity) throws Exception {
		var segment = query.getSegment();

		var entity = this.find(segment);

		if (entity != null) {
			if (query.hasNext()) {
				// 由于表达式还未完，所以在下个节点中继续执行
				entity.setMember(query.getNext(), createEntity);
			} else {
				// 覆盖，此处并没有改变name
				var index = _members.indexOf(entity);
				var e = createEntity.apply(segment);
				e.setParent(this);
				_members.set(index, e);
			}
		} else {
			// 没有找到
			if (query.hasNext()) {

				var next = obtain();
				next.setName(segment);
				next.setParent(this);
				_members.add(next);

				next.setMember(query.getNext(), createEntity);

			} else {

				// 没有后续
				var e = createEntity.apply(segment);
				e.setParent(this);
				_members.add(e);
			}
		}
	}

	@Override
	public void removeMember(DTEntity e) {
		_members.remove(e);
	}

	@Override
	public String getCode(boolean sequential, boolean outputName) throws Exception {
		return getCodeImpl(sequential, outputName, (member) -> {
			return member.getCode(sequential, true);
		});
	}

	@Override
	public String getSchemaCode(boolean sequential, boolean outputName) throws Exception {
		return getCodeImpl(sequential, outputName, (member) -> {
			return member.getSchemaCode(sequential, true);
		});
	}

	/**
	 * 是否为{value}的形式
	 * 
	 * @return
	 */
	public boolean isSingleValue() {
		if (isNullOrEmpty(this.getName()) && _members.size() == 1) {
			var member = as(_members.get(0), DTEValue.class);
			return member != null && isNullOrEmpty(member.getName());
		}
		return false;
	}

	private String getCodeImpl(boolean sequential, boolean outputName, Func1<DTEntity, String> getMemberCode)
			throws Exception {
		String name = this.getName();
		var isSingleValue = this.isSingleValue();
		return StringPool.using((code) -> {

			if (outputName && !isNullOrEmpty(name))
				code.append(String.format("\"%s\"", name));

			if (code.length() > 0)
				code.append(":");

			if (isSingleValue) {
				code.append(getMemberCode.apply(_members.get(0)));
			} else {
				code.append("{");

				if (sequential) {
					usingList((items) -> {
						// 排序输出
						items.addAll(_members);
						items.sort((t1, t2) -> {
							return t1.getName().compareTo(t2.getName());
						});
						fillCode(code, items, getMemberCode);
					});
				} else {
					fillCode(code, _members, getMemberCode);
				}

				if (StringUtil.last(code) == ',')
					StringUtil.removeLast(code);
				code.append("}");
			}
		});
	}

	private static void fillCode(StringBuilder code, Iterable<DTEntity> items, Func1<DTEntity, String> getMemberCode)
			throws Exception {
		for (var item : items) {
			var memberCode = getMemberCode.apply(item);
			code.append(memberCode);
			code.append(",");
		}
	}

	private static Pool<DTEObject> pool = new Pool<DTEObject>(() -> {
		return new DTEObject();
	}, PoolConfig.onlyMaxRemainTime(300));

	public static DTEObject obtain() {
		var item = ContextSession.obtainItem(pool, () -> new DTEObject());
		item.setMembers(DTEntity.obtainList());
		return item;
	}

	public static DTEObject obtain(ArrayList<DTEntity> members) {
		var item = ContextSession.obtainItem(pool, () -> new DTEObject());
		item.setMembers(members);
		return item;
	}

}
