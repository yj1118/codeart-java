package subsystem.saga;

public enum NodeStatus {

    SUCCESS((byte) 1),

    ERROR_BEFORE((byte) 2),

    ERROR_AFTER((byte) 3);

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
            default -> null;
        };
    }

}