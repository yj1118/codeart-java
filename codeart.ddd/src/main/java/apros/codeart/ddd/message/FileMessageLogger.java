package apros.codeart.ddd.message;

class FileMessageLogger implements IMessageLog {

	private FileMessageLogger() {
	}

	public static final FileMessageLogger instance = new FileMessageLogger();

}
