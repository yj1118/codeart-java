package apros.codeart.context;

import apros.codeart.pooling.IReusable;

public interface IAppSession extends IReusable {

	Object getItem(String name);

	void setItem(String name, Object value);

	boolean containsItem(String name);
}
