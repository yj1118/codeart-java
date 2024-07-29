package apros.codeart.ddd.repository.db;

import java.text.MessageFormat;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.GeneratedField;
import apros.codeart.ddd.repository.access.GetAssociatedQB;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class GetAssociated extends GetAssociatedQB {
    private GetAssociated() {
    }

    @Override
    protected String buildImpl(DataTable table) {
        return MessageFormat.format("select {0} from {1} where {2}=@{3} and {4}=@{5};",
                SqlStatement.qualifier(GeneratedField.AssociatedCountName),
                SqlStatement.qualifier(table.name()),
                SqlStatement.qualifier(GeneratedField.RootIdName),
                GeneratedField.RootIdName,
                SqlStatement.qualifier(EntityObject.IdPropertyName),
                EntityObject.IdPropertyName);
    }

    public static final GetAssociated Instance = new GetAssociated();

}