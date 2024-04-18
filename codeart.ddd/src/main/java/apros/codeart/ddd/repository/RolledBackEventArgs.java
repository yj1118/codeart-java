package apros.codeart.ddd.repository;

public class RolledBackEventArgs {

	private DataContext _context;

	public DataContext context() {
		return _context;
	}

	public RolledBackEventArgs(DataContext context) {
		_context = context;
	}
}
