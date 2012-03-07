package pin.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.liteProto.LlpJava;

public class NetService {

	private int port;
	private ChannelPipelineFactory channelPipelineFactory;
	public NetService(int port, ChannelPipelineFactory channelPipelineFactory) {
		this.port = port;
		this.channelPipelineFactory = channelPipelineFactory;
	}
	
    public void run() {
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(channelPipelineFactory);

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
        System.out.println("server started ...");
    }
}
