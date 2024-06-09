package apros.codeart.dto;

import static apros.codeart.i18n.Language.strings;

import java.util.ArrayList;
import java.util.function.Function;

import com.google.common.collect.Iterables;

import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ArgumentAssert;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;
import org.jetbrains.annotations.NotNull;

class AssignExpression extends TransformExpression {
	private final String _findExp;
	private final String _valueFindExp;

	public AssignExpression(String exp) {
		var temp = exp.split("=");
		_findExp = StringUtil.trim(temp[0]);
		_valueFindExp = StringUtil.trim(temp[1]);
	}

	@Override
	public void execute(DTObject dto) {
		execute(dto, (v) -> {
			return v;
		});
	}

	public void execute(DTObject dto, Function<Object, Object> transformValue) {
		changeValue(dto, _findExp, _valueFindExp, transformValue);
	}

	//region 更改值

	/**
	 *  该方法用于更改成员的值
	 * @param dto 需要更改的dto对象
	 * @param findExp
	 * @param valueFindExp
	 * @param transformValue
	 */
	public void changeValue(@NotNull DTObject dto, @NotNull String findExp, @NotNull String valueFindExp,
							Function<Object, Object> transformValue) {
		ArgumentAssert.isNotNullOrEmpty(findExp, "findExp");
		ArgumentAssert.isNotNullOrEmpty(valueFindExp, "valueFindExp");

		// 1.找出需要赋值的目标成员
		var targets = dto.finds(findExp, false);
		if (Iterables.size(targets) == 0)
			dto.setString(findExp, StringUtil.empty()); // 如果没有成员，就自动生成
		targets = dto.finds(findExp, false);

		var valueExpression = _getValueExpression.apply(valueFindExp);

		for (var target : targets) {
			var parent = TypeUtil.as(target.getParent(), DTEObject.class);
			if (parent == null)
				throw new IllegalArgumentException(strings("apros.codeart", "UnknownException"));

			var parentDTO = valueExpression.getStartRoot() ? dto : new DTObject(parent, dto.isReadOnly());

			// 2.找出值，值是在目标成员所在的对象下进行查找的
			var entities = parentDTO.finds(valueExpression.getFindExp(), false);
			var entitiesCount = Iterables.size(entities);
			if (entitiesCount == 1) {
				// 获取值
				var ve = Iterables.get(entities, 0);
				var newValue = getValue(ve, transformValue, dto.isReadOnly());
				// if (newValue == null) throw new DTOException("预期之外的数据转换，" +
				// valueExpression.FindExp);
				if (newValue == null)
					continue; // 传递的值是null，就表明调用者要忽略这条数据

				// 目标值是唯一的，这个时候要进一步判断
				var valueObjParent = TypeUtil.as(ve.getParent().getParent(), DTEList.class); // 这是值所在的对象的父亲
				// if (valueObjFather != null && ve!=target)
				// //如果值所在的对象处在集合中，并且不是自身对自身赋值，那么还是要以集合形式赋值
				if (valueObjParent != null && !ve.getParent().equals(target.getParent())) // 如果值所在的对象处在集合中，并且不是自身对象对自身对象赋值，那么还是要以集合形式赋值
				{
					// 以集合赋值
					setValue(target, new Object[] { newValue }, valueExpression.getFindExp());
				} else {
					// 赋单值
					setValue(target, newValue, valueExpression.getFindExp());
				}
			} else if (entitiesCount > 1) {
				// 如果目标值是多个，那么是集合类型，这时候需要收集所有转换后的值，再赋值
				ArrayList<Object> values = new ArrayList<Object>(entitiesCount);
				for (var e : entities) {
					var newValue = getValue(e, transformValue, dto.isReadOnly());
					if (newValue == null)
						throw new IllegalArgumentException(strings("apros.codeart", "UnknownException"));
					values.add(newValue);
				}

				setValue(target, values, valueExpression.getFindExp());
			} else {
				// 值为0,需要判断是否为数组
				var path = _getFindExpPath.apply(valueExpression.getFindExp());
				boolean isArray = false;
				for (var exp : path) {
					var ent = dto.find(exp, false);
					if (ent == null)
						break;
					isArray = TypeUtil.is(ent, DTEList.class);
					if (isArray)
						break;
				}
				if (isArray)
					setValue(target, new Object[] {}, valueExpression.getFindExp());
				else {
					var newValue = transformValue.apply(null);
					setValue(target, newValue, valueExpression.getFindExp());
				}

			}
		}
	}

	private void setValue(DTEntity target, Object value, String findExp) {
		var entityValue = TypeUtil.as(target, DTEValue.class);
		if (entityValue != null) {
			entityValue.setValueRef(value, Util.getValueCodeIsString(value));
			return;
		}

		var parent = TypeUtil.as(target.getParent(), DTEObject.class);
		if (parent == null)
			throw new IllegalArgumentException(strings("apros.codeart", "ExpressionError", findExp));

		var query = QueryExpression.create(target.getName());
		parent.setMember(query, (name) -> {
			var dtoValue = TypeUtil.as(value, DTObject.class);
			if (dtoValue != null) {
				var t = dtoValue.clone();
				var newEntity = t.getRoot();
				newEntity.setName(name);
				return newEntity;
			} else {
				DTObject t = DTObject.editable();
				t.setValue(value);
				var newEntity = t.getRoot().first();
				if (newEntity == null)
					throw new IllegalArgumentException(strings("apros.codeart", "UnknownException"));
				newEntity.setName(name);
				return newEntity;
			}
		});
	}

	private Object getValue(DTEntity e, Function<Object, Object> transformValue, boolean isReadOnly) {
		var ve = TypeUtil.as(e, DTEValue.class);
		if (ve != null)
			return transformValue.apply(ve.getValueRef());

		var le = TypeUtil.as(e, DTEList.class);
		if (le != null) {
			var list = le.getObjects();
			var value = ListUtil.map(list, (item) -> {
				if (item.isSingleValue())
					return item.getValue();
				return item;
			});
			return transformValue.apply(value);
		}

		var oe = TypeUtil.as(e, DTEObject.class);
		if (oe != null) {
			var value = new DTObject(oe, isReadOnly);
			return transformValue.apply(value);
		}
		return null;
	}

	//endregion

	private static Function<String, String[]> _getFindExpPath = LazyIndexer.init((valueFindExp) -> {
		return valueFindExp.split(".");
	});

	private static Function<String, ValueTuple> _getValueExpression = LazyIndexer.init((valueFindExp) -> {
		boolean startRoot = valueFindExp.startsWith("@");
		if (startRoot)
			valueFindExp = StringUtil.substr(valueFindExp, 1);
		return new ValueTuple(startRoot, valueFindExp);
	});

	private static class ValueTuple {

		private boolean _startRoot;

		public boolean getStartRoot() {
			return _startRoot;
		}

		private String _findExp;

		public String getFindExp() {
			return _findExp;
		}

		public ValueTuple(boolean startRoot, String findExp) {
			_startRoot = startRoot;
			_findExp = findExp;
		}
	}
}
