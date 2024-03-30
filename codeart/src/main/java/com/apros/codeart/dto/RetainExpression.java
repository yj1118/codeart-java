package com.apros.codeart.dto;

import static com.apros.codeart.i18n.Language.strings;

import java.util.ArrayList;
import java.util.List;

import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

class RetainExpression extends TransformExpression {
	private Iterable<String> _findExps;

	public RetainExpression(String exp) {
		_findExps = ListUtil.map(StringUtil.substr(exp, 1).split(","), (temp) -> {
			return StringUtil.trim(temp);
		});
	}

	@Override
	public void execute(DTObject dto) {
		retainEntities(dto, _findExps);
	}

//	#region 保留成员

	private static class ReservedInfo {

		private DTEntity _entity;

		public DTEntity getEntity() {
			return _entity;
		}

		/**
		 * 是否为完整保留
		 */
		private boolean _isComplete;

		public boolean isComplete() {
			return _isComplete;
		}

		public ReservedInfo(DTEntity entity, boolean isComplete) {
			_entity = entity;
			_isComplete = isComplete;
		}

	}

	private void retainEntities(DTObject dto, Iterable<String> findExps) {
		var size = Iterables.size(findExps);
		if (size == 0)
			return;

		// 收集需要保留的实体
		List<ReservedInfo> targets = new ArrayList<ReservedInfo>(size);
		for (var findExp : findExps) {
			var items = dto.finds(findExp, false);
			for (var item : items) {
				targets.add(new ReservedInfo(item, true));
				// 加入自身
				// 再加入父亲，由于自身需要保留，所以父亲也得保留
				var parent = item.getParent();
				while (parent != null) {
					targets.add(new ReservedInfo(parent, false));
					parent = parent.getParent();
				}
			}
		}

		var removes = new ArrayList<DTEntity>();
		collectNeedRemove(dto.getRoot().getMembers(), targets, removes);

		for (var t : removes) {
			var parent = t.getParent();
			if (parent == null)
				throw new IllegalArgumentException(strings("UnknownException"));
			parent.removeMember(t);
		}

	}

	private void collectNeedRemove(Iterable<DTEntity> members, List<ReservedInfo> reservedRemoves,
			List<DTEntity> removes) {
		for (var member : members) {
			var findItem = ListUtil.find(reservedRemoves, (t) -> {
				return t.getEntity().equals(member);
			});

			if (findItem == null) {
				// 不在保留列表中，那么加入删除列表
				removes.add(member);
			} else {
				if (findItem.isComplete())
					continue; // 如果是完整匹配，那么子项也会保留

				// 在保留列表中，继续判断子项是否保留
				var obj = TypeUtil.as(member, DTEObject.class);
				if (obj != null) {
					collectNeedRemove(obj.getMembers(), reservedRemoves, removes);
				} else {
					var list = TypeUtil.as(member, DTEList.class);
					if (list != null) {

						var childs = ListUtil.map(list.getObjects(), (t) -> {
							return (DTEntity) t.getRoot();
						});
						collectNeedRemove(childs, reservedRemoves, removes);
					}
				}
			}
		}
	}

//	#endregion
}
