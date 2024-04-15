package com.apros.codeart.ddd.repository.sqlserver;

import java.text.MessageFormat;

import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.repository.access.DataTable;
import com.apros.codeart.ddd.repository.access.GeneratedField;
import com.apros.codeart.ddd.repository.access.GetAssociatedQB;
import com.apros.codeart.util.SafeAccess;

@SafeAccess
class GetAssociated extends GetAssociatedQB {
	private GetAssociated() {
	}

	@Override
	protected String buildImpl(DataTable table) {
		return MessageFormat.format("select [{0}] from [{1}] where [{2}]=@{2} and [{3}]=@{3};",
				GeneratedField.AssociatedCountName, table.name(), GeneratedField.RootIdName,
				EntityObject.IdPropertyName);
	}

	public static final GetAssociated Instance = new GetAssociated();

}