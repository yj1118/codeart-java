package apros.codeart.ddd.saga;

import apros.codeart.i18n.Language;

import java.util.concurrent.ConcurrentHashMap;

public enum EventStatus {

    None((byte) 0),
    Raising((byte) 1),
    Reversing((byte) 2);
    private final byte _value;

    EventStatus(byte value) {
        this._value = value;
    }

    public byte value() {
        return _value;
    }

    public byte getValue() {
        return _value;
    }

    public static EventStatus valueOf(byte value) {
        for (var item : EventStatus.values()) {
            if (item.value() == value) {
                return item;
            }
        }
        throw new IllegalArgumentException(Language.strings("apros.codeart.ddd", "NoEnum", value));
    }

    private static final ConcurrentHashMap<String, EventStatus> _eventStatus = new ConcurrentHashMap<>();

    public static void setStatus(String eventId, EventStatus status) {
        _eventStatus.put(eventId, status);
    }

    public static void removeStatus(String eventId) {
        _eventStatus.remove(eventId);
    }

    public static EventStatus getStatus(String eventId) {
        return _eventStatus.getOrDefault(eventId, EventStatus.None);
    }


}
