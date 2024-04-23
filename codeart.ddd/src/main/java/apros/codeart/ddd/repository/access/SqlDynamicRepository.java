package apros.codeart.ddd.repository.access;

import static apros.codeart.i18n.Language.strings;

import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.dynamic.IDynamicRepository;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class SqlDynamicRepository implements IDynamicRepository {

	private SqlDynamicRepository() {
	}

	public <T extends DynamicRoot> T find(Class<T> rootType, Object id, QueryLevel level) {
		var model = DataModelLoader.get(rootType);
		return model.querySingle(id, level);
	}

	public static final SqlDynamicRepository Instance = new SqlDynamicRepository();

	@Override
	public IAggregateRoot findRoot(Object id, QueryLevel level) {
		throw new UnsupportedOperationException("SqlDynamicRepository.findRoot(object id, QueryLevel level)");
	}

	@Override
	public void addRoot(IAggregateRoot obj) {
		var root = asRoot(obj);
		var model = DataModelLoader.get(obj.getClass());
		model.insert(root);

	}

	@Override
	public void updateRoot(IAggregateRoot obj) {
		var root = asRoot(obj);
		var model = DataModelLoader.get(obj.getClass());
		model.update(root);
	}

	@Override
	public void deleteRoot(IAggregateRoot obj) {
		var root = asRoot(obj);
		var model = DataModelLoader.get(obj.getClass());
		model.delete(root);
	}

	private DynamicRoot asRoot(IAggregateRoot obj) {
		var root = TypeUtil.as(obj, DynamicRoot.class);
		if (root == null) {
			throw new IllegalArgumentException(
					strings("codeart.ddd", "NotDynamicRootType", obj.getClass().getSimpleName()));
		}
		return root;
	}

}
