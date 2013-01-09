package pin.net;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

public class NetService {
    private Logger logger = LoggerFactory.getLogger(NetService.class);
    private int port;
    private ChannelPipelineFactory channelPipelineFactory;
    private Map<String, Object> options;

    /**
     * 创建网络服务
     *
     * @param port                   监听端口
     * @param channelPipelineFactory {@link ChannelPipelineFactory}
     */
    public NetService(int port, ChannelPipelineFactory channelPipelineFactory, Map<String, Object> options) {
        this.port = port;
        this.channelPipelineFactory = channelPipelineFactory;
        this.options = options;
    }

    public NetService(int port, ChannelPipelineFactory channelPipelineFactory) {
        this(port, channelPipelineFactory, null);
    }

    /**
     * 开启网络服务
     */
    public void run() {
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newFixedThreadPool(4),
                Executors.newFixedThreadPool(8)));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(channelPipelineFactory);

        if(options != null && options.size() > 0) {
            bootstrap.setOptions(options);
        }

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
        logger.info("server started on port " + port + "...");
    }
}
