package su.jacob.socks;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ProxyServerHandler.class);

    private static final String XML_PATTERN = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    private static final String XML_TO_SEND = "<?xml version=\"1.0\" encoding=\"utf-8\"?><cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"*\" /></cross-domain-policy>\0";

    private ProxyClient proxyClient;
    private ScheduledExecutorService scheduledService;
    private ExecutorService clientExecutorService;

    public ProxyServerHandler(String remoteIP, int remotePort) {
        this.proxyClient = new ProxyClient(remoteIP, remotePort);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        this.scheduledService = Executors.newScheduledThreadPool(1);
        this.scheduledService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] responseBytes = proxyClient.getNewMessageReceived();
                    if (responseBytes != null) {
                        logger.debug("server has msg found....................................................................................");
                        int respBufLength = responseBytes.length;
                        if (respBufLength > 350) {
                            ctx.write(Unpooled.wrappedBuffer(responseBytes));
                        } else {
                            String responseString = new String(responseBytes, CharsetUtil.UTF_8);
                            if (responseString.contains(XML_PATTERN)) {
                                int index = responseString.indexOf(XML_PATTERN);
                                responseString = responseString.substring(0, index);
                                responseString += XML_TO_SEND;
                                ByteBuf finalBuf = Unpooled.wrappedBuffer(responseString.getBytes(CharsetUtil.UTF_8));
                                ctx.write(finalBuf);
                            } else {
                                ctx.write(Unpooled.wrappedBuffer(responseBytes));
                            }
                        }
                        ctx.flush();
                    } else {
                        logger.debug("server has no msg found....................................................................................");
                    }
                } catch (Exception ex) {
                    logger.error("server channel active error: ", ex);
                    ex.printStackTrace();
                }
            }
        }, 0L, 10L, TimeUnit.MILLISECONDS);
        this.clientExecutorService = Executors.newSingleThreadExecutor();
        this.clientExecutorService.submit(this.proxyClient);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf inBuffer = (ByteBuf) msg;
        proxyClient.addMessageToSend(ByteBufUtil.getBytes(inBuffer));
        logger.debug("server has msg inbound....................................................................................");
    }



    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.scheduledService.shutdownNow();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}