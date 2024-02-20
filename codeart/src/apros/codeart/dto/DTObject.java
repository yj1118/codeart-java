package apros.codeart.dto;

public class DTObject {
	public DTObject clone() {
		return null;
//        return new DTObject(_root.Clone() as DTEObject, this.IsReadOnly);
	}

	public String getCode() {
		return getCode(false, false);
	}

	public String getCode(boolean sequential, boolean outputName) {
		return null;
	}

	void fillCode(StringBuilder code) {

	}

}
