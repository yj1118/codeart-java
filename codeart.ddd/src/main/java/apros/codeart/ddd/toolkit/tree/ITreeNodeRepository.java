package apros.codeart.ddd.toolkit.tree;

public interface ITreeNodeRepository {
    void move(TreeNode<?> current, TreeNode<?> target);
}