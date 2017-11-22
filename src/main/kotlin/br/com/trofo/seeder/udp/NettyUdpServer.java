package br.com.trofo.seeder.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class NettyUdpServer {

    @Value("${server.udp.port}")
    private int udpPort;

    @Autowired
    private UdpHandler udpHandler;

    @PostConstruct
    public void createUdpServer() throws Exception {
        //TODO Check best way to start netty server on separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup group = new NioEventLoopGroup();
                try {
                    Bootstrap b = new Bootstrap();
                    b.group(group)
                            .channel(NioDatagramChannel.class)
                            .handler(udpHandler);

                    b.bind(udpPort).sync().channel().closeFuture().await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    group.shutdownGracefully();
                }
            }
        }).start();
    }
}