package apros.codeart.ddd.repository.db;

import java.text.MessageFormat;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.GeneratedField;
import apros.codeart.ddd.repository.access.IncrementAssociatedQB;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
public class IncrementAssociated extends IncrementAssociatedQB {
    private IncrementAssociated() {
    }

    @Override
    protected String buildImpl(DataTable table) {
        return StringUtil.format("update {0} set {3}={3}+1 where {1}=@{1} and {2}=@{2};",
                SqlStatement.qualifier(table.name()),
                SqlStatement.qualifier(GeneratedField.RootIdName),
                SqlStatement.qualifier(EntityObject.IdPropertyName),
                SqlStatement.qualifier(GeneratedField.AssociatedCountName));
    }

    public static final IncrementAssociated Instance = new IncrementAssociated();

}
