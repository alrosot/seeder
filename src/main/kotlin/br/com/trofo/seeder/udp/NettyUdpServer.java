package br.com.trofo.seeder.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class NettyUdpServer {

    private static ChannelFuture channel;

    @Value("${server.udp.port}")
    private int udpPort;

    @Autowired
    private UdpHandler udpHandler;
    private Logger LOG = LoggerFactory.getLogger(NettyUdpServer.class);

    @PreDestroy
    public void destroy() throws InterruptedException {
        LOG.info("Shutting down Netty server");
        channel.channel().close();
        channel.channel().closeFuture().sync();
    }

    @PostConstruct
    public synchronized void createUdpServer() throws Exception {
        if (channel == null) {
            LOG.info("Starting Netty Server");
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(udpHandler);

            channel = b.bind(udpPort).sync();
        }
    }
}