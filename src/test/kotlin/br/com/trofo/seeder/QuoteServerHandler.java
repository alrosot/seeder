package br.com.trofo.seeder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.tomcat.util.buf.HexUtils;

import java.util.Arrays;

public class QuoteServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.content();

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        // WIP http://www.bittorrent.org/beps/bep_0015.html
        System.out.println(HexUtils.toHexString(bytes));
        byte[] transactionId = Arrays.copyOfRange(bytes, 12, 16);
        byte[] response = new byte[16];
        System.arraycopy(transactionId, 0, response, 4, 4);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(response), msg.sender()));
    }
}
