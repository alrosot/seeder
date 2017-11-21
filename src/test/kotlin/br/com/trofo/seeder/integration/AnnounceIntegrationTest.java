package br.com.trofo.seeder.integration;

import br.com.trofo.seeder.dao.PeerRepository;
import org.apache.tomcat.util.buf.HexUtils;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnnounceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PeerRepository peerRepository;

    @Before
    public void cleanUp() {
        peerRepository.deleteAll();
    }

    @Test
    public void announceNewTorrent() throws UnsupportedEncodingException {
        assertThat(getInfoHashes(), is("[]"));
        String hexInfoHash = "aaacccbbbdddc723b738fd8b895c953a5d22ba7b";
        verifyRegistration(hexInfoHash);
        verifyRegistration(hexInfoHash); // should not duplicate
        assertThat(getInfoHashes(), is("[\"" + hexInfoHash + "\"]"));
        verifyRegistration("%FE%AF%0D%0D%B5%1D%C7%23%B78%FD%8B%89%5C%95%3A%5D%22%BA%7B", "feaf0d0db51dc723b738fd8b895c953a5d22ba7b");
    }

    @Test
    public void shouldConnect() throws Exception {
        String connectionId = startNewConnection();
        // Should get new connectionId every time
        assertThat(connectionId, is(not(startNewConnection())));
    }


    @Test
    public void shoudAnnounceOverUdop() throws Exception {
        String connectionId = "f56350d9cf7c3735";
        String action = "00000001";
        String infohash = "feaf0d0db51dc723b738fd8b895c953a5d22ba7b";
        String port = "2327";
        String event = "00000002";
        String peerId = "2d7142333347302d684e545f6b59746865214e5a";
        String transactionId = "b77e0246";
        String payload = connectionId + action + transactionId + infohash + peerId + "000000000019f24700000000050400000000000000000000" + event + "00000000fcd454b2000000c8" + port + "02092f616e6e6f756e6365";

        byte[] buf = HexUtils.fromHexString(payload);
        InetAddress address = InetAddress.getLoopbackAddress();
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        DatagramSocket socket = new DatagramSocket();
        packet.setAddress(address);
        packet.setPort(8090);
        socket.send(packet);

        assertThat(getInfoHashes(), is("[\"" + infohash + "\"]"));

        DatagramPacket responsePacket = new DatagramPacket(new byte[30], 30);
        socket.receive(responsePacket);
        System.out.println("Udp response: " + HexUtils.toHexString(responsePacket.getData()));

        String interval = "00000e10";
        assertThat(HexUtils.toHexString(responsePacket.getData()), is("00000001" + transactionId + interval
                + "00000001" + "00000001" + HexUtils.toHexString(InetAddress.getLoopbackAddress().getAddress()) + port
        ));

    }


    @NotNull
    private String startNewConnection() throws IOException {
        byte[] buf = HexUtils.fromHexString("000004172710198000000000abcddcba");
        assertThat(buf.length, is(16));
        InetAddress address = InetAddress.getLoopbackAddress();
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        DatagramSocket socket = new DatagramSocket();
        packet.setAddress(address);
        packet.setPort(8090);
        socket.send(packet);

        socket.receive(packet);

        String hexString = HexUtils.toHexString(packet.getData());
        assertThat(hexString, Matchers.startsWith(("00000000abcddcba")));
        String connectionId = hexString.substring(16);
        assertThat(connectionId.length(), is(16));
        return connectionId;
    }

    private void verifyRegistration(String infoHash) {
        verifyRegistration(infoHash, infoHash);
    }

    private void verifyRegistration(String rawInfoHash, String readableInfoHash) {
        restTemplate.getForEntity(URI.create("/announce?info_hash=" + rawInfoHash), String.class);
        String body = getInfoHashes();
        assertThat(body, Matchers.containsString(readableInfoHash));
    }

    private String getInfoHashes() {
        return restTemplate.getForEntity(URI.create("/info_hash"), String.class).getBody();
    }
}
