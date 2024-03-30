package com.apros.codeart.dto;

import static com.apros.codeart.i18n.Language.strings;

import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringUtil;

class RemoveExpression extends TransformExpression {
	private Iterable<String> _findExps;

	public RemoveExpression(String exp) {
		_findExps = ListUtil.map(StringUtil.substr(exp, 1).split(","), (temp) -> {
			return StringUtil.trim(temp);
		});
	}

	@Override
	public void execute(DTObject dto) {
		for (var findExp : _findExps) {
			removeEntities(dto, findExp);
		}
	}

	private void removeEntities(DTObject dto, String findExp) {
		var targets = dto.finds(findExp, false);
		for (var target : targets) {
			var parent = target.getParent();
			if (parent == null)
				throw new IllegalArgumentException(strings("UnknownException"));
			parent.removeMember(target);
		}
	}
}
