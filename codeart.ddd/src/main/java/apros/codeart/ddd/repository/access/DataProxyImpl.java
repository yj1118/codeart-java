package apros.codeart.ddd.repository.access;

import apros.codeart.ddd.DataProxy;
import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.repository.DataContext;

class DataProxyImpl extends DataProxy {

    private MapData _originalData;

    /**
     * 从数据库中加载的数据
     * 我们并不在数据代理中的原始数据里包含附加字段的值，因为这些值由程序员额外维护，我们不能保证对象缓冲区的对象中包含的数据和附加字段的数据是同步的
     * 例如：在树状结构中，批量更改了LR的值，这时候数据缓冲区并不知道
     *
     * @return
     */
    public MapData originalData() {
        return _originalData;
    }

    DataTable _table;

    /**
     * 对象对应的数据表
     *
     * @return
     */
    public DataTable table() {
        return _table;
    }

    public DataProxyImpl(MapData originalData, DataTable table, boolean isMirror) {
        _originalData = originalData;
        _table = table;
        _isMirror = isMirror;
    }

    @Override
    protected Object loadData(String propertyName) {

        var tip = PropertyMeta.getProperty(this.getOwner().getClass(), propertyName);

        if (tip != null) {
            var level = isLoadByMirroring() ? QueryLevel.MIRRORING : QueryLevel.NONE;
            return this.table().readPropertyValue(this.getOwner(), tip, null, this.originalData(), level);
        }
        return null;
    }

    /**
     * 镜像对象的属性加载优先用镜像模式 如果处于提交阶段，那么使用无锁模式，在提交阶段加载数据一般是由于验证器的验证操作引起的
     *
     * @return
     */
    private boolean isLoadByMirroring() {
        if (!DataContext.existCurrent())
            return false;
        var context = DataContext.getCurrent();
        return !context.isCommiting() && this.getOwner().isMirror();
    }

    @Override
    public boolean isSnapshot() {

        // 通过对比数据版本号判定数据是否为快照
        var current = this.getVersion();
        var latest = this.table().getDataVersion(this.originalData());
        return current != latest; // 当对象已经被删除，对象版本号大于数据库版本号，当对象被修改，当前对象版本号小于数据库版本号
    }

    private boolean _isMirror;

    @Override
    public boolean isMirror() {
        return _isMirror;
    }

    @Override
    public int getVersion() {
        return (int) this.originalData().get(GeneratedField.DataVersionName);
    }

    @Override
    public void setVersion(int value) {
        this.originalData().put(GeneratedField.DataVersionName, value);
    }

    @Override
    public void syncVersion() {
        var version = this.table().getDataVersion(this.originalData());
        this.setVersion(version);
    }
}
