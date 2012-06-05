package pin.event;

/**
 * 事件监听者
 * @author zhongyuan
 *
 */
public interface EventListener {
	/**
	 * 相应事件
	 * @param sender 事件发送者(事件源)
	 * @param event 事件参数
	 */
	void fireEvent(Object sender, EventArgs event);
}
