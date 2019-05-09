package su.jacob.socks;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Jacob.Su
 * @mail: jacob.su@emc.com
 */
public class ProxyClient extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ProxyClient.class);

    private String remoteIP;
    private int remotePort;

    private Queue<byte[]> msqToSendQueue;
    private Queue<byte[]> msgReceivedQueue;

    public ProxyClient(String remoteIP, int remotePort) {
        this.remoteIP = remoteIP;
        this.remotePort = remotePort;
        this.msqToSendQueue = new LinkedBlockingQueue<>();
        this.msgReceivedQueue = new LinkedBlockingQueue<>();
    }

    public void addMessageToSend(byte[] msg) {
        this.msqToSendQueue.add(msg);
    }

    public byte[] getNewMessageReceived() {
        if (this.msgReceivedQueue.isEmpty()) {
            return null;
        } else {
            return this.msgReceivedQueue.remove();
        }
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap clientBootstrap = new Bootstrap();

            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress(this.remoteIP, this.remotePort));
            clientBootstrap.handler(new ProxyChannelInitializer(this.msqToSendQueue,  this.msgReceivedQueue));

            Channel channel = clientBootstrap.connect().sync().channel();
            logger.info("client started to ip: " + this.remoteIP + ", port: " + this.remotePort + "....................................................................................");
            while (true) {
                Thread.sleep(10);
                if(! this.msqToSendQueue.isEmpty()){
                    logger.debug("client msg sent....................................................................................");
                    ByteBuf msgToSend = Unpooled.wrappedBuffer(this.msqToSendQueue.remove());
                    channel.writeAndFlush(msgToSend);
                }
            }

//            ChannelFuture channelFuture = clientBootstrap.connect().sync();
//            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            new RuntimeException(e);
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }
}
