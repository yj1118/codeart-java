package com.apros.codeart.dto;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.TypeUtil.as;
import static com.apros.codeart.util.StringUtil.isNullOrEmpty;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.apros.codeart.context.ContextSession;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

public final class DTEObject extends DTEntity {

	private boolean _readonly;

	private AbstractList<DTEntity> _members;

	private void setMembers(AbstractList<DTEntity> membres) {
		_members = membres;
		for (var e : _members) {
			e.setParent(this);
		}
	}

	private DTEObject(boolean readonly, String name, AbstractList<DTEntity> members) {
		_readonly = readonly;
		setName(name);
		setMembers(members);
	}

	public Iterable<DTEntity> getMembers() {
		return Collections.unmodifiableList(_members);
	}

	void addMember(DTEntity e) {
		e.setParent(this);
		_members.add(e);
	}

	public DTEntity first() {
		return ListUtil.first(_members);
	}

	@Override
	public DTEntity cloneImpl() {
		var items = Util.<DTEntity>createList(_readonly, _members.size());
		for (var e : _members) {
			items.add((DTEntity) e.clone());
		}

		return obtain(_readonly, this.getName(), items);
	}

	@Override
	public void load(DTEntity entity) {
		var o = TypeUtil.as(entity, DTEObject.class);
		if (o == null)
			throw new IllegalArgumentException(strings("TypeMismatch"));

		var targetMembers = o.getMembers();
		var targetSize = Iterables.size(targetMembers);

		var selfSize = _members.size();

		// 用迭代器遍历，可以兼容arrayList和LinkedList的性能，避免使用get(int index)方法导致链表项取性能低下的问题
		var selfIterator = _members.iterator();
		var targetIterator = targetMembers.iterator();

		if (selfSize < targetSize) {
			var i = 0;
			while (i < selfSize) {
				var se = selfIterator.next();
				var te = targetIterator.next();
				se.load(te);
				i++;
			}

			while (i < targetSize) { // 补充缺的项
				var te = targetIterator.next();
				addMember((DTEntity) te.clone());
				i++;
			}
		} else {
			var i = 0;
			while (i < targetSize) {
				var se = selfIterator.next();
				var te = targetIterator.next();
				se.load(te);
				i++;
			}

			while (i < selfSize) { // 删除多的项
				var se = selfIterator.next();
				selfIterator.remove();
				se.setParent(null);
				se.clearData();
				i++;
			}
		}

	}

//	@Override
//	public void close() throws Exception {
//		super.close();
//		// 这里只用断开与数组的连接，不用清理members
//		// 因为members都被注册了，会话结束时会被清理
//		_members = null;
//	}

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
		return ListUtil.removeFirst(_members, (t) -> t.getName().equalsIgnoreCase(memberName)) != null;
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
	public Iterable<DTEntity> finds(QueryExpression query) {
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
		return Collections.emptyList();
	}

	/**
	 * 设置成员，当成员存在时覆盖，成员不存在时新增
	 * 
	 * @throws Exception
	 */
	@Override
	public void setMember(QueryExpression query, Function<String, DTEntity> createEntity) {
		var segment = query.getSegment();

		var entity = this.find(segment);

		if (entity != null) {
			if (query.hasNext()) {
				// 由于表达式还未完，所以在下个节点中继续执行
				entity.setMember(query.getNext(), createEntity);
			} else {
				// 覆盖，此处并没有改变name
				ListUtil.set(_members, (s) -> s.equals(entity), () -> {
					var e = createEntity.apply(segment);
					e.setParent(this);
					return e;
				});
			}
		} else {
			// 没有找到
			if (query.hasNext()) {
				var next = obtainEditable(segment);
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
	public void fillCode(StringBuilder code, boolean sequential, boolean outputName) {
		fillCodeImpl(code, sequential, (c, name) -> {
			if (outputName && !isNullOrEmpty(name))
				code.append(String.format("\"%s\"", name));
		}, (c, member) -> {
			member.fillCode(c, sequential, true);
		});
	}

	public String getCode(boolean sequential, boolean outputName) {
		StringBuilder sb = new StringBuilder();
		fillCode(sb, sequential, outputName);
		return sb.toString();
	}

	@Override
	public void fillSchemaCode(StringBuilder code, boolean sequential) {
		fillCodeImpl(code, sequential, (c, name) -> {
			if (!isNullOrEmpty(name))
				code.append(name);
		}, (c, member) -> {
			member.fillSchemaCode(c, sequential);
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

	private void fillCodeImpl(StringBuilder code, boolean sequential, BiConsumer<StringBuilder, String> outputName,
			BiConsumer<StringBuilder, DTEntity> fillMemberCode) {
		String name = this.getName();

		outputName.accept(code, name);

		if (code.length() > 0)
			code.append(":");

		if (this.isSingleValue()) {
			fillMemberCode.accept(code, _members.get(0));
		} else {
			code.append("{");

			if (sequential) {
				// 排序输出
				var items = new ArrayList<DTEntity>(_members.size());
				items.addAll(_members);
				items.sort((t1, t2) -> {
					return t1.getName().compareTo(t2.getName());
				});
				fillCode(code, items, fillMemberCode);
			} else {
				fillCode(code, _members, fillMemberCode);
			}

			if (StringUtil.last(code) == ',')
				StringUtil.removeLast(code);
			code.append("}");
		}
	}

	private static void fillCode(StringBuilder code, Iterable<DTEntity> items,
			BiConsumer<StringBuilder, DTEntity> fillMemberCode) {
		for (var item : items) {
			fillMemberCode.accept(code, item);
			code.append(",");
		}
	}

	/**
	 * 调用此方法获得一个可编辑的对象
	 * 
	 * @param name
	 * @return
	 */
	public static DTEObject obtainEditable(String name) {
		return ContextSession.registerItem(new DTEObject(false, name, new LinkedList<DTEntity>()));
	}

	/**
	 * 
	 * 空的，且只读的
	 * 
	 * @param name
	 * @return
	 */
	public static DTEObject obtainReadonlyEmpty(String name) {
		return new DTEObject(true, name, new ArrayList<DTEntity>());
//		return ContextSession.registerItem(new DTEObject(true, name, new ArrayList<DTEntity>()));
	}

	public static DTEObject obtain(boolean readonly, String name, AbstractList<DTEntity> members) {
		return new DTEObject(readonly, name, members);
//		return ContextSession.registerItem(new DTEObject(readonly, name, members));
	}

}
