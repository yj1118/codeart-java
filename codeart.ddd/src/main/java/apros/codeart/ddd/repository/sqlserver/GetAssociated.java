package apros.codeart.ddd.repository.sqlserver;

import java.text.MessageFormat;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.GeneratedField;
import apros.codeart.ddd.repository.access.GetAssociatedQB;
import apros.codeart.util.SafeAccess;

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