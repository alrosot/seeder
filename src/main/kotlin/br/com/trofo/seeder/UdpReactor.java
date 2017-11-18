package br.com.trofo.seeder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.io.encoding.StandardCodecs;
import reactor.net.netty.udp.NettyDatagramServer;
import reactor.net.tcp.support.SocketUtils;
import reactor.net.udp.DatagramServer;
import reactor.net.udp.spec.DatagramServerSpec;
import reactor.spring.context.config.EnableReactor;

@Configuration
@EnableAutoConfiguration
@EnableReactor
@ComponentScan
public class UdpReactor {

    private Log log = LogFactory.getLog(UdpReactor.class);

    @Bean
    public DatagramServer<byte[], byte[]> datagramServer(Environment env) throws InterruptedException {

        final DatagramServer<byte[], byte[]> server = new DatagramServerSpec<byte[], byte[]>(NettyDatagramServer.class)
                .env(env)
                .listen(SocketUtils.findAvailableTcpPort())
                .codec(StandardCodecs.BYTE_ARRAY_CODEC)
                .consumeInput(bytes -> log.info("received: " + new String(bytes)))
                .get();

        server.start().await();
        return server;
    }

}
