package subsystem.saga;

import apros.codeart.dto.DTObject;

public class Accumulator {

    private int _value;

    private Accumulator(int value) {
        _value = value;
    }

    public int value() {
        return _value;
    }

    public void addUp() {
        _value++;
    }

    public void decrease() {
        _value--;
    }

    public void setValue(int value) {
        _value = value;
    }

    public DTObject toDTO() {
        var result = DTObject.editable();
        result.setInt("value", this.value());
        return result;
    }


    public static final Accumulator Instance = new Accumulator(0);

}
