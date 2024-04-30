package apros.codeart.ddd.saga.internal.protector;

import apros.codeart.dto.DTObject;

final class EventListener {
	private EventListener() {
	}

	/**
	 * 被要求回逆，这意味着收到回逆的指令
	 */
	public static void reverse(DTObject e) {
		var id = e.getString("id");
		// true 表示是后台接收到的回溯命令，不是用户操作导致的
		EventProtector.restore(id);
	}

}
