package apros.codeart.ddd.repository.sqlserver;

import java.text.MessageFormat;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.GeneratedField;
import apros.codeart.ddd.repository.access.IncrementAssociatedQB;
import apros.codeart.util.SafeAccess;

@SafeAccess
class IncrementAssociated extends IncrementAssociatedQB {
	private IncrementAssociated() {
	}

	@Override
	protected String buildImpl(DataTable table) {
		return MessageFormat.format("update [{0}] set [{3}]=[{3}]+1 where [{1}]=@{1} and [{2}]=@{2};", table.name(),
				GeneratedField.RootIdName, EntityObject.IdPropertyName, GeneratedField.AssociatedCountName);
	}

	public static final IncrementAssociated Instance = new IncrementAssociated();

}
