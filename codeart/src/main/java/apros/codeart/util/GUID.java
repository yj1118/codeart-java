package apros.codeart.util;

import java.util.UUID;

public final class GUID {

    private GUID() {
    }

    public static final UUID Empty = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * 获得不带分隔符的guid,32个字符串的长度
     *
     * @return
     */
    public static String compact() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    public static UUID empty() {
        return Empty;
    }

    public static boolean isNullOrEmpty(UUID value) {
        return value == null || value.equals(Empty);
    }

    public static UUID newGuid() {
        return UUID.randomUUID();
    }

}
