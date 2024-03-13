package com.apros.codeart.dto;

import com.apros.codeart.util.ArgumentAssert;
import com.apros.codeart.util.StringUtil;

class ChangeNameExpression extends TransformExpression {
	private String _findExp;
	private String _name;

	public ChangeNameExpression(String exp) {
		int index = exp.indexOf("=>"); // 转换成员名称
		if (index > 0) {

			_findExp = StringUtil.substr(exp, 0, index);
			_findExp = StringUtil.trim(_findExp);

			_name = StringUtil.substr(exp, index + 2);
			_name = StringUtil.trim(_name);
		}

		ArgumentAssert.isNotNullOrEmpty(_findExp, "findExp");
		ArgumentAssert.isNotNullOrEmpty(_name, "name");
	}

	@Override
	public void execute(DTObject dto) {
		var entities = dto.finds(_findExp, false);
		for (var e : entities) {
			e.setName(_name);
		}
	}
}
