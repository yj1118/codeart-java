package apros.codeart.dto;

import static apros.codeart.i18n.Language.strings;

public class DTOReadonlyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1779637237807968875L;

	public DTOReadonlyException() {
		super(strings("apros.codeart", "DTOReadOnly"));
	}

}
