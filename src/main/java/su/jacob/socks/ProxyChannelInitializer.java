package su.jacob.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.Queue;

/**
 * @author Jacob.Su
 * @mail: jacob.su@emc.com
 */
public class ProxyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Queue<byte[]> msqToSendQueue;
    private Queue<byte[]> msgReceivedQueue;

    public ProxyChannelInitializer(Queue<byte[]> msqToSendQueue, Queue<byte[]> msgReceivedQueue) {
        this.msqToSendQueue = msqToSendQueue;
        this.msgReceivedQueue = msgReceivedQueue;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new ProxyClientHandler(this.msqToSendQueue,  this.msgReceivedQueue));
    }
}
