package apros.codeart.ddd;

public class DomainPropertyChangeEventArgs {

    private final Object _newValue;

    public Object newValue() {
        return _newValue;
    }

    private final Object _oldValue;

    public Object oldValue() {
        return _oldValue;
    }

    private final String _propertyName;

    /**
     * 获取发生值更改的领域项属性的标识符
     *
     * @return
     */
    public String propertyName() {
        return _propertyName;
    }

    public DomainPropertyChangeEventArgs(String propertyName, Object newValue, Object oldValue) {
        this._propertyName = propertyName;
        this._newValue = newValue;
        this._oldValue = oldValue;
    }
}
