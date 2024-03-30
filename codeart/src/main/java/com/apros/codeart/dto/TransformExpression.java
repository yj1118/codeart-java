package com.apros.codeart.dto;

import java.util.function.Function;

import com.apros.codeart.i18n.Language;
import com.apros.codeart.util.LazyIndexer;

abstract class TransformExpression {
	public abstract void execute(DTObject target);

//	#region 静态成员

	private static Function<String, TransformExpression> _getExpression = LazyIndexer.init((transformString) -> {
		int index = transformString.indexOf("=>"); // 转换成员名称

		if (index > 0) {
			return new ChangeNameExpression(transformString);
		}

		index = transformString.indexOf("="); // 赋值
		if (index > 0) {
			return new AssignExpression(transformString);
		}

		index = transformString.indexOf("!"); // 移除表达式对应的成员
		if (index == 0) {
			return new RemoveExpression(transformString);
		}

		index = transformString.indexOf("~"); // 保留表达式对应的成员，其余的均移除
		if (index == 0) {
			return new RetainExpression(transformString);
		}

		throw new IllegalArgumentException(Language.strings("TransformExpressionError"));
	});

	public static TransformExpression create(String transformString) {
		return _getExpression.apply(transformString);
	}

//	#endregion
}
