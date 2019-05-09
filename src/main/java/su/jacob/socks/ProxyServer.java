package su.jacob.socks;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author Jacob.Su
 * @mail: jacob.su@emc.com
 */
public class ProxyServer  {

    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);

    private String remoteIp;
    private int remotePort;
    private String localIp;
    private int localPort;

    public ProxyServer(String remoteIp, int remotePort, String localIp, int localPort) {
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.localIp = localIp;
        this.localPort = localPort;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ProxyServerHandler(remoteIp, remotePort));
                        }
                    })
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_KEEPALIVE,true);
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(this.localIp, this.localPort)).sync();
            logger.info("server started....................................................................................");
            channelFuture.channel().closeFuture().sync();
            logger.info("server closed....................................................................................");
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            logger.debug("server resource released....................................................................................");
        }
    }
}
