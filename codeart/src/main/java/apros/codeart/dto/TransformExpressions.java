package apros.codeart.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;
import org.jetbrains.annotations.NotNull;

class TransformExpressions implements Iterable<TransformExpression> {
    private final ArrayList<TransformExpression> _expressions;

    private TransformExpressions(String transformString) {

        var itemCodes = ListUtil.map(transformString.split(";"), StringUtil::trim).stream().filter((temp) -> {
            return !StringUtil.isNullOrEmpty(temp);
        }).toArray();

        _expressions = new ArrayList<TransformExpression>(itemCodes.length);

        for (var i = 0; i < itemCodes.length; i++) {
            var itemCode = itemCodes[i].toString();
            _expressions.add(TransformExpression.create(itemCode));
        }
    }

    private static final Function<String, TransformExpressions> _getExpression = LazyIndexer.init(TransformExpressions::new);

    public static TransformExpressions create(String transformString) {
        return _getExpression.apply(transformString);
    }

    @Override
    public @NotNull Iterator<TransformExpression> iterator() {
        return _expressions.iterator();
    }

}
