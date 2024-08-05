package apros.codeart.ddd.repository.access.internal;

public final class SqlLike {
    private byte _position;

    public boolean before() {
        return (_position & Position.Before.getValue()) == Position.Before.getValue();
    }

    /**
     * 是否前置匹配
     *
     * @param value
     * @return
     */
    private void setBefore(boolean value) {
        _position &= ~Position.Before.getValue();
        if (value)
            _position |= Position.Before.getValue();
    }

    public boolean after() {
        return (_position & Position.After.getValue()) == Position.After.getValue();
    }

    /**
     * 是否后置通配
     *
     * @param value
     * @return
     */
    private void setAfter(boolean value) {
        _position &= ~Position.After.getValue();
        if (value)
            _position |= Position.After.getValue();
    }

    private final String _paramName;

    /**
     * 匹配的参数名称
     *
     * @return
     */
    public String paramName() {
        return _paramName;
    }

    private final String _fieldName;

    public String fieldName() {
        return _fieldName;
    }

    SqlLike(String fieldName, String paramName, boolean before, boolean after) {
        _fieldName = fieldName;
        _paramName = paramName;
        setBefore(before);
        setAfter(after);
    }

    private enum Position {

        // 前置通配
        Before((byte) 0x1),

        //		后置通配
        After((byte) 0x2);

        private final byte value;

        // 构造器，为每个枚举值设置byte值
        private Position(byte value) {
            this.value = value;
        }

        // 公开方法，允许外部访问枚举的byte值
        public byte getValue() {
            return value;
        }

    }

}
