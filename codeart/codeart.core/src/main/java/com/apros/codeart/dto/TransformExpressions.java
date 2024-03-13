package com.apros.codeart.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;

import com.apros.codeart.util.LazyIndexer;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringUtil;

class TransformExpressions implements Iterable<TransformExpression> {
	private ArrayList<TransformExpression> _expressions;

	private TransformExpressions(String transformString) {

		var itemCodes = ListUtil.map(transformString.split(";"), (temp) -> {
			return StringUtil.trim(temp);
		}).stream().filter((temp) -> {
			return !StringUtil.isNullOrEmpty(temp);
		}).toArray();

		_expressions = new ArrayList<TransformExpression>(itemCodes.length);

		for (var i = 0; i < itemCodes.length; i++) {
			var itemCode = itemCodes[i].toString();
			_expressions.add(TransformExpression.create(itemCode));
		}
	}

	private static Function<String, TransformExpressions> _getExpression = LazyIndexer.init((transformString) -> {
		return new TransformExpressions(transformString);
	});

	public static TransformExpressions create(String transformString) {
		return _getExpression.apply(transformString);
	}

	@Override
	public Iterator<TransformExpression> iterator() {
		return _expressions.iterator();
	}

}
