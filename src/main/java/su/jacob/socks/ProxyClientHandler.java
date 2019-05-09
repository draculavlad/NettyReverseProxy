package su.jacob.socks;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * @author Jacob.Su
 * @mail: jacob.su@emc.com
 */
public class ProxyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(ProxyClientHandler.class);

    private Queue<byte[]> msqToSendQueue;
    private Queue<byte[]> msgReceivedQueue;

    public ProxyClientHandler(Queue<byte[]> msqToSendQueue, Queue<byte[]> msgReceivedQueue) {
        this.msqToSendQueue = msqToSendQueue;
        this.msgReceivedQueue = msgReceivedQueue;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        this.msgReceivedQueue.add(ByteBufUtil.getBytes(in));
        logger.debug("client msg received....................................................................................");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause){
        cause.printStackTrace();
        channelHandlerContext.close();
    }
}
