package br.com.trofo.seeder.udp;

import br.com.trofo.seeder.PeerService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Random;

import static java.lang.System.arraycopy;

@Component
public class UdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static byte[] connectionMagicNumber = HexUtils.fromHexString("0000041727101980");
    private Random generator = new Random();
    @Autowired
    private PeerService peerService;

    @Override
    /**
     * @see <a href="http://www.bittorrent.org/beps/bep_0015.html"></a>
     */
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        byte[] bytes = getMessageBytes(msg);
        System.out.println(HexUtils.toHexString(bytes));
        byte[] firstEightBytes = Arrays.copyOfRange(bytes, 0, 8);
        if (Arrays.equals(firstEightBytes, connectionMagicNumber)) {
            acceptConnectionRequest(ctx, msg, bytes);
        } else {
            String infoHash = HexUtils.toHexString(Arrays.copyOfRange(bytes, 16, 36));
            String ip = HexUtils.toHexString(msg.sender().getAddress().getAddress());
            int port = (bytes[96] << 8) | (bytes[97] & 0x00ff);
            peerService.registerPeer(infoHash, ip, port);
            //TODO reply with peers
        }
    }

    private void acceptConnectionRequest(ChannelHandlerContext ctx, DatagramPacket msg, byte[] bytes) {
        byte[] transactionId = Arrays.copyOfRange(bytes, 12, 16);
        byte[] response = new byte[16];
        arraycopy(transactionId, 0, response, 4, 4);
        byte[] connectionId = new byte[8];
        generator.nextBytes(connectionId);
        arraycopy(connectionId, 0, response, 8, 8);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(response), msg.sender()));
    }

    private byte[] getMessageBytes(DatagramPacket msg) {
        ByteBuf buf = msg.content();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }
}
