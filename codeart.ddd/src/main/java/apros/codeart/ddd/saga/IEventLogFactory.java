package apros.codeart.ddd.saga;

public interface IEventLogFactory {

	void init();

	IEventLog create();
}