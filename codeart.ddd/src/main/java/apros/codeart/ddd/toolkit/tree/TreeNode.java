package apros.codeart.ddd.toolkit.tree;

import apros.codeart.ddd.*;
import apros.codeart.ddd.repository.PropertyRepository;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.ddd.toolkit.OrderItemLong;
import apros.codeart.ddd.validation.List;
import apros.codeart.ddd.validation.NotEmpty;
import apros.codeart.dto.DTObject;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * 在做操作时,请根据业务自行锁根
 *
 * @param <T>
 */
@MergeDomain
@FrameworkDomain
public abstract class TreeNode<T extends TreeNode<T>> extends AggregateRootLong {

    public static final String NodeRootPropertyName = "NodeRoot";

    public static final String ParentPropertyName = "Parent";

    public static final String ChildrenPropertyName = "Children";

    //region 根节点

    @PropertyRepository(lazy = true)
    @NotEmpty()
    public static final DomainProperty NodeRootProperty = DomainProperty.register("NodeRoot", TreeNode.class, TreeNode.class);

    @SuppressWarnings("unchecked")
    public T nodeRoot() {
        return (T) this.getValue(NodeRootProperty, TreeNode.class);
    }

    private void nodeRoot(T value) {
        this.setValue(NodeRootProperty, value);
    }

    public boolean isRoot() {
        return this.equals(this.nodeRoot());
    }

    //endregion

    //region 父节点

    @PropertyRepository(lazy = true)
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
            this.level(0);  // 根是0级
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

//    void sortChildren(Class<T> nodeClass, Iterable<OrderItemLong> items) {
//
//        for (var item : items) {
//            var obj = Repository.find(nodeClass, item.id(), QueryLevel.NONE);
//            obj.orderIndex(item.orderIndex());
//            Repository.update(obj);
//        }
//
//        this._children().sort(Comparator.comparing(T::orderIndex));
//    }

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

//    void removeChild(long childId, T empty) {
//        var child = this.getChild(childId, empty);
//        if (child.isEmpty()) return;
//
//        this._children().remove(child);
//
//        Repository.delete(child);
//    }

    public static <T extends TreeNode<T>> void remove(Class<T> type, long id) {
        //在做操作时,请根据业务自行锁根
        var obj = Repository.find(type, id, QueryLevel.NONE);
        if (obj.isEmpty()) return;
        var parent = obj.parent();
        if (!parent.isEmpty())
            parent.removeChild(obj);
        removeSelf(obj);
        if (!parent.isEmpty())
            Repository.update(parent);
    }

    private static <T extends TreeNode<T>> void removeSelf(T obj) {
        if (obj.isEmpty()) return;
        for (var child : obj.children()) {
            removeSelf(child);
        }
        // 直接删除
        Repository.delete(obj);
    }

    public void sort(long[] ids) {

        var type = this.getClass();

        for (var i = 0; i < ids.length; i++) {
            var id = ids[i];
            var obj = Repository.find(type, id, QueryLevel.NONE);
            obj.orderIndex(i);
            Repository.update(obj);
        }

        this._children().sort(Comparator.comparing(TreeNode::orderIndex));  //排序并不影响左右值计算
    }

//    internal static void RemoveChild(Competitor competitor, long parentId, long id)
//    {
//        var repository = Repository.Create<IFunctionRepository>();
//
//        var parent = GetParent(competitor, parentId, repository);
//
//        parent.RemoveChild(id);
//
//        repository.Update(parent);
//    }

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
        if (this.isEmpty() || target.isEmpty()) return;
        if (this.parent().equals(target)) return;
        if (target.id() == this.id() || this.isChild(target.id()))
            throw new BusinessException("This operation is not supported");

        var repository = Repository.createByObjectType(objectType);

        var oldParent = this.parent();
        oldParent.removeChild((T) this);

        this.parent(target);
        var newParent = this.parent();
        this.level(newParent.level() + 1);
        newParent.addChild((T) this);

        // 需要用仓储来管理this、parent、target三者改变后，左右值的同步
        {
            var tnRepository = TypeUtil.as(Repository.createByObjectType(objectType), ITreeNodeRepository.class);
            tnRepository.move(this, target);
        }

        repository.updateRoot(oldParent);
        repository.updateRoot(newParent);

        if (this.childrenCount() > 0) handleChildrenLevel();
    }


    public DTObject output(String schemaCode, Predicate<T> predicate) {

        if (predicate != null && !predicate.test((T) this)) return DTObject.empty();

        DTObject data = DTObject.readonly(schemaCode, this);

        for (var child : this.children()) {
            var item = child.output(schemaCode);
            if (item.isEmpty()) continue;
            data.push("children", item);
        }
        return data;
    }

    public DTObject output(String schemaCode) {
        return output(schemaCode, null);
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

    @Override
    public void onAdded() {
        super.onAdded();
        if (!this.isRoot())
            Repository.update(this.parent());
    }

    public TreeNode(long id) {
        super(id);
        this.onConstructed();
    }

    @SuppressWarnings("unchecked")
    public TreeNode(long id, T parent) {
        super(id);
        this.setParent(parent);
        if (parent.isEmpty()) this.nodeRoot((T) this);
        else this.nodeRoot(parent.nodeRoot());
        this.onConstructed();
    }
}
