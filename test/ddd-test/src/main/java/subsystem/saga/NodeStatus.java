package subsystem.saga;

public enum NodeStatus {

    SUCCESS((byte) 1),

    ERROR_BEFORE((byte) 2),

    ERROR_AFTER((byte) 3),

    TIMEOUT_BEFORE((byte) 4),
    TIMEOUT_AFTER((byte) 5),

    Unplugged_BEFORE((byte) 6),
    Unplugged_AFTER((byte) 7);

    private final byte value;

    NodeStatus(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }

    public static NodeStatus valueOf(byte value) {
        return switch (value) {
            case 1 -> SUCCESS;
            case 2 -> ERROR_BEFORE;
            case 3 -> ERROR_AFTER;
            case 4 -> TIMEOUT_BEFORE;
            case 5 -> TIMEOUT_AFTER;
            case 6 -> Unplugged_BEFORE;
            case 7 -> Unplugged_AFTER;
            default -> null;
        };
    }

}