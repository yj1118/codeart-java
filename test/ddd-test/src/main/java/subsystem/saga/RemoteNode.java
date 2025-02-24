package subsystem.saga;

import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;

public final class RemoteNode {

    private final String _eventName;

    public String eventName() {
        return _eventName;
    }

    private final NodeStatus _status;

    public NodeStatus status() {
        return _status;
    }

    public RemoteNode(String eventName, NodeStatus status) {
        _eventName = eventName;
        _status = status;
    }

    public DTObject to() {
        var dto = DTObject.editable("{eventName}", this);
        dto.setByte("status", this.status().getValue());
        return dto;
    }

    public static RemoteNode from(DTObject node) {
        var eventName = node.getString("eventName");
        var status = NodeStatus.valueOf(node.getByte("status"));
        return new RemoteNode(eventName, status);
    }

    public static final RemoteNode[] empty = new RemoteNode[]{};

    public static RemoteNode[] getNodes(DTObject dto) {
        var nodes = dto.getList("remoteNodes", false);
        if (nodes == null) return empty;

        var temp = ListUtil.map(nodes, RemoteNode::from);

        return ListUtil.asArray(temp, RemoteNode.class);

    }
}



