package br.com.trofo.seeder.udp;

import br.com.trofo.seeder.PeerService;
import br.com.trofo.seeder.entity.Peer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;

import static br.com.trofo.seeder.PeerEvent.getByUdpCode;
import static java.lang.System.arraycopy;
import static org.apache.commons.lang3.StringUtils.leftPad;

@Component
public class UdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final byte[] connectionMagicNumber = HexUtils.fromHexString("0000041727101980");
    private final Random generator = new Random();
    private final PeerService peerService;

    @Autowired
    public UdpHandler(PeerService peerService) {
        this.peerService = peerService;
    }

    /* According to bit torrent specification available at
      @see <a href="http://www.bittorrent.org/beps/bep_0015.html"></a>
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        byte[] bytes = getMessageBytes(msg);
        byte[] firstEightBytes = Arrays.copyOfRange(bytes, 0, 8);
        if (Arrays.equals(firstEightBytes, connectionMagicNumber)) {
            acceptConnectionRequest(ctx, msg, bytes);
        } else {
            announceResponse(ctx, msg, bytes);
        }
    }

    private void announceResponse(ChannelHandlerContext ctx, DatagramPacket msg, byte[] bytes) {
        String infoHash = HexUtils.toHexString(Arrays.copyOfRange(bytes, 16, 36));
        byte[] clientIpAddress = Arrays.copyOfRange(bytes, 84, 88);
        if (Arrays.equals(clientIpAddress, new byte[4])) {
            clientIpAddress = msg.sender().getAddress().getAddress();
        }
        String ip = HexUtils.toHexString(clientIpAddress);
        String event = HexUtils.toHexString(Arrays.copyOfRange(bytes, 80, 84));
        int port = (bytes[96] << 8) | (bytes[97] & 0x00ff);
        Collection<Peer> peers = peerService.registerPeer(infoHash, ip, port, Optional.of(getByUdpCode(event)));

        StringBuilder sb = new StringBuilder();

        sb.append("00000001"); // action - 1 announce
        sb.append(HexUtils.toHexString(Arrays.copyOfRange(bytes, 12, 16))); // transaction id

        sb.append(intToHex(PeerService.INTERVAL, 8));
        sb.append(intToHex(peers.size(), 8)); // leechers
        sb.append(intToHex(peers.size(), 8)); // seeders

        for (Peer seeder : peers) {
            sb.append(seeder.getIp());
            sb.append(intToHex(seeder.getPort(), 4));
        }

        String input = sb.toString();
        byte[] response = HexUtils.fromHexString(input);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(response), msg.sender()));
    }

    private String intToHex(Integer number, int padding) {
        return leftPad(Integer.toHexString(number), padding, '0');
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
