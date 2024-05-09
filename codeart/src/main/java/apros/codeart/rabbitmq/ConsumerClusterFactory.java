package apros.codeart.rabbitmq;

import java.util.ArrayList;

public final class ConsumerClusterFactory {

	private ConsumerClusterFactory() {
	}

	public static Iterable<IConsumerCluster> getAll() {
		return _clusters;
	}

	private static ArrayList<IConsumerCluster> _clusters = new ArrayList<IConsumerCluster>();

	public static void add(IConsumerCluster cluster) {
		_clusters.add(cluster);
	}

}
