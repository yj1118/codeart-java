package apros.codeart.ddd.metadata;

import apros.codeart.i18n.Language;

public enum DomainObjectCategory {

    AggregateRoot((byte) 1), EntityObject((byte) 2), ValueObject((byte) 3);

    private final byte _value;

    DomainObjectCategory(byte value) {
        this._value = value;
    }

    public byte value() {
        return _value;
    }

    public byte getValue() {
        return _value;
    }

    public static DomainObjectCategory valueOf(byte value) {
        for (var item : DomainObjectCategory.values()) {
            if (item.value() == value) {
                return item;
            }
        }
        throw new IllegalArgumentException(Language.strings("apros.codeart.ddd", "NoEnum", value));
    }
}
