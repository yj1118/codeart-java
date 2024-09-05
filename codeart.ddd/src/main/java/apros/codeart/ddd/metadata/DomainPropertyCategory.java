package apros.codeart.ddd.metadata;

import apros.codeart.i18n.Language;

public enum DomainPropertyCategory {

    Primitive((byte) 1), PrimitiveList((byte) 2), ValueObject((byte) 3), AggregateRoot((byte) 4),
    EntityObject((byte) 5), ValueObjectList((byte) 6), EntityObjectList((byte) 7), AggregateRootList((byte) 8);

    private final byte _value;

    DomainPropertyCategory(byte value) {
        this._value = value;
    }

    public byte value() {
        return _value;
    }

    public byte getValue() {
        return _value;
    }

    public static DomainPropertyCategory valueOf(byte value) {
        for (var item : DomainPropertyCategory.values()) {
            if (item.value() == value) {
                return item;
            }
        }
        throw new IllegalArgumentException(Language.strings("apros.codeart.ddd", "NoEnum", value));
    }

}
