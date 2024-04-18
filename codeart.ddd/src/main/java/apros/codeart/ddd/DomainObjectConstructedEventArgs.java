package apros.codeart.ddd;

public class DomainObjectConstructedEventArgs {

	private DomainObject _source;

	public DomainObject source() {
		return _source;
	}

	public DomainObjectConstructedEventArgs(DomainObject source) {
		_source = source;
	}

}
