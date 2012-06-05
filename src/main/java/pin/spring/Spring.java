package pin.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Spring {
	private static Spring instance = new Spring();
	private ApplicationContext context;

	/**
	 * 私有构造函数
	 */
	private Spring() {

	}

	/**
	 * 获得spring实例
	 * 
	 * @return
	 */
	public static Spring instance() {
		return instance;
	}

	/**
	 * 初始化Srping对象 从XML中初始化ApplicationContext
	 * 
	 * @param confXMLs
	 *            spring xml配置文件
	 */
	public void init(String... confXMLs) {
		context = new ClassPathXmlApplicationContext(confXMLs);
	}

	/**
	 * 从Spring 中获取对象
	 * @param name 定义的对象名称
	 * @param clazz 对象Class
	 * @return 获取的对象
	 */
	public <T> T getBean(String name, Class<T> clazz) {
		return context.getBean(name, clazz);
	}
}
