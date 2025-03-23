package apros.codeart.ddd.toolkit.tree;

import apros.codeart.ddd.*;
import apros.codeart.ddd.repository.PropertyRepository;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.ddd.toolkit.OrderItemLong;
import apros.codeart.ddd.validation.List;
import apros.codeart.ddd.validation.NotEmpty;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.MethodUtil;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.runtime.Util;
import apros.codeart.util.ListUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

@MergeDomain
@FrameworkDomain
public abstract class TreeNode<T extends TreeNode<T>> extends AggregateRootLong {

    public static final String RootPropertyName = "Root";

    public static final String ParentPropertyName = "Parent";

    public static final String ChildrenPropertyName = "Children";

    //region 根节点

    @PropertyRepository(lazy = true)
    @NotEmpty()
    public static final DomainProperty RootProperty = DomainProperty.register("Root", TreeNode.class, TreeNode.class);

    @SuppressWarnings("unchecked")
    public T root() {
        return (T) this.getValue(RootProperty, TreeNode.class);
    }

    private void root(T value) {
        this.setValue(RootProperty, value);
    }

    //endregion

    //region 父节点

    @PropertyRepository(lazy = true)
    @NotEmpty()
    public static final DomainProperty ParentProperty = DomainProperty.register("Parent", TreeNode.class, TreeNode.class);

    @SuppressWarnings("unchecked")
    public T parent() {
        return (T) this.getValue(ParentProperty, TreeNode.class);
    }

    private void parent(T value) {
        this.setValue(ParentProperty, value);
    }

    @SuppressWarnings("unchecked")
    public void setParent(T value) {
        if (value == null) return;

        if (value.isEmpty()) {
            this.parent(value);
            this.level(1);
        } else {
            boolean existChild = ListUtil.contains(value.children(), (c) -> c.equals(value));

            if (!existChild) {
                value._children().add((T) this);
                this.parent(value);
                this.level(this.parent().level() + 1);
            }
        }
    }

    /**
     * 获得该节点所属的所有父节点
     *
     * @return
     */
    public Iterable<T> getParents() {
        var parent = this.parent();
        if (parent.level() == 1) return ListUtil.empty();
        var parents = new ArrayList<T>();
        parents.add(parent);
        ListUtil.addRange(parents, parent.getParents());
        return parents;
    }


    //endregion

    //region 子项

    @PropertyRepository(lazy = true)
    @List(max = 100)
    public static final DomainProperty ChildrenProperty = DomainProperty.registerCollection("Children", TreeNode.class, TreeNode.class);

    @SuppressWarnings("unchecked")
    DomainCollection<T> _children() {
        return (DomainCollection<T>) this.getValue(ChildrenProperty);
    }

//    public void children(Iterable<T> value) {
//        this.setValue(ChildrenProperty,
//                new DomainCollection<T>(TreeNode.class, ChildrenProperty, value));
//    }

    public Iterable<T> children() {
        return _children();
    }

    void sortChildren(Class<T> nodeClass, Iterable<OrderItemLong> items) {

        for (var item : items) {
            var obj = Repository.find(nodeClass, item.id(), QueryLevel.NONE);
            obj.orderIndex(item.orderIndex());
            Repository.update(obj);
        }

        this._children().sort(Comparator.comparing(T::orderIndex));
    }

    public int childrenCount() {
        return _children().size();
    }


    public T getChild(long childId, T empty) {
        return Objects.requireNonNullElse(ListUtil.find(this.children(), c -> c.id() == childId), empty);
    }

    void addChild(T child) {
        if (!ListUtil.contains(this.children(), c -> c.id() == child.id())) {
            this._children().add(child);
        }
    }

    void removeChild(T child) {
        _children().remove(child);
    }

    void removeChild(long childId, T empty) {
        var child = this.getChild(childId, empty);
        if (child.isEmpty()) return;

        this._children().remove(child);

        Repository.delete(child);
    }


    /**
     * 判断targetId目录是否为当前节点的子节点
     *
     * @param childId
     * @return
     */
    public boolean isChild(long childId) {
        return ListUtil.contains(this.children(), c -> c.id() == childId);
    }

    //endregion

    @PropertyRepository
    public static final DomainProperty LevelProperty = DomainProperty.register("Level", int.class,
            TreeNode.class);

    public int level() {
        return this.getValue(LevelProperty, int.class);
    }

    void level(int value) {
        this.setValue(LevelProperty, value);
    }

    @PropertyRepository
    public static final DomainProperty LeftProperty = DomainProperty.register("Left", int.class,
            TreeNode.class);

    public int left() {
        return this.getValue(LeftProperty, int.class);
    }

    private void left(int value) {
        this.setValue(LeftProperty, value);
    }

    @PropertyRepository
    public static final DomainProperty RightProperty = DomainProperty.register("Right", int.class,
            TreeNode.class);

    public int right() {
        return this.getValue(RightProperty, int.class);
    }

    private void right(int value) {
        this.setValue(RightProperty, value);
    }


    @PropertyRepository
    public static final DomainProperty OrderIndexProperty = DomainProperty.register("OrderIndex", int.class,
            TreeNode.class);

    public int orderIndex() {
        return this.getValue(OrderIndexProperty, int.class);
    }

    public void orderIndex(int value) {
        this.setValue(OrderIndexProperty, value);
    }

    @PropertyRepository
    public static final DomainProperty MovingProperty = DomainProperty.register("Moving", boolean.class, TreeNode.class);

    public boolean moving() {
        return this.getValue(MovingProperty, boolean.class);
    }

    private void moving(boolean value) {
        this.setValue(MovingProperty, value);
    }

    @SuppressWarnings("unchecked")
    public void move(Class<T> objectType, T target) {
        if (this.isEmpty()) return;
        if (target.id() == this.id() || this.isChild(target.id()))
            throw new BusinessException("This operation is not supported");

        var repository = Repository.createByObjectType(objectType);

        var oldParent = this.parent();
        oldParent.removeChild((T) this);

        this.parent(target);
        var newParent = this.parent();
        this.level(newParent.level() + 1);

        repository.updateRoot(oldParent);

        // 需要用仓储来管理this、parent、target三者改变后，左右值的同步
        {
            var tnRepository = TypeUtil.as(Repository.createByObjectType(objectType), ITreeNodeRepository.class);
            tnRepository.move(this, target);
        }

        if (this.childrenCount() > 0) handleChildrenLevel();
    }

    public DTObject output() {
        DTObject data = DTObject.readonly("{id,name,level}", this);

        for (var child : this.children()) {
            var item = child.output();
            data.push("children", item);
        }
        return data;
    }


    /**
     * 更新级别
     */
    void handleChildrenLevel() {
        for (var item : this.children()) {
            item.level(this.level() + 1);
            Repository.update(item);
            if (item.childrenCount() > 0) item.handleChildrenLevel();
        }
    }

//
//    Department Create(Department obj)
//    {
//        if (obj.IsEmpty()) return Department.Empty;
//        if (obj.Parent.IsEmpty()) obj.Parent = this;
//
//        var repository = Repository.Create<IDepartmentRepository>();
//        repository.Add(obj);
//        repository.Update(obj.Parent);
//        return obj;
//    }


//    internal void Delete(Department obj)
//    {
//        if (obj.IsEmpty()) return;
//        obj.Parent.RemoveChild(obj);
//
//        var repository = Repository.Create<IDepartmentRepository>();
//        repository.Update(obj.Parent);
//        repository.Delete(obj);
//    }
//
//    public static T createRoot(long tenantId)
//    {
//        long id = DataPortal.GetIdentity<Department>();
//        var repository = Repository.Create<IDepartmentRepository>();
//        var obj = new Department(id, "根部门", Department.Empty);
//        repository.Add(obj);
//        return obj;
//    }

    public TreeNode(long id) {
        super(id);
        this.onConstructed();
    }

    public TreeNode(long id, T parent) {
        super(id);
        this.setParent(parent);
        this.onConstructed();
    }
}
