package apros.codeart.util;

public final class WrapperBoolean {

    private boolean _value;

    public WrapperBoolean(boolean value){
        _value = value;
    }

    public boolean getValue(){
        return _value;
    }

    public void setValue(boolean value){
        _value = value;
    }

}
