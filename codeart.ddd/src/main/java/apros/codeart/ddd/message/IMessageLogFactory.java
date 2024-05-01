package apros.codeart.ddd.message;

public interface IMessageLogFactory {
	void init();

	IMessageLog create();
}
