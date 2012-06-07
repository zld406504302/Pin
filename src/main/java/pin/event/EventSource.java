package pin.event;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件源
 * @author zhongyuan
 *
 */
public class EventSource {
	
	/**
	 * 监听者容器
	 */
	private List<EventListener> listeners = new ArrayList<EventListener>();
	
	/**
	 * 添加监听者
	 * @param listener
	 */
	public void addListener(EventListener listener) {
		listeners.add(listener);
	}
	
	public void fireEvent(Object sender, EventArgs event) {
		for(EventListener listener : listeners) {
			listener.fireEvent(sender, event);
		}
	}
}
