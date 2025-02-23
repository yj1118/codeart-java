package subsystem.saga;

public enum NodeStatus {
    
    ERROR((byte) 0),

    SUCCESS((byte) 1);

    private final byte value;

    NodeStatus(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }

    public static NodeStatus valueOf(byte value) {
        switch (value) {
            case 0:
                return ERROR;
            case 1:
                return SUCCESS;
        }
        return null;
    }

}