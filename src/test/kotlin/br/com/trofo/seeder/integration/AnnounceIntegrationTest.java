package br.com.trofo.seeder.integration;

import br.com.trofo.seeder.dao.PeerRepository;
import br.com.trofo.seeder.entity.Peer;
import org.apache.tomcat.util.buf.HexUtils;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Date;
import java.util.Random;

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
        String hexInfoHash = randomInfoHash();
        verifyRegistration(hexInfoHash);
        verifyRegistration(hexInfoHash); // should not duplicate
        assertThat(getInfoHashes(), is("[\"" + hexInfoHash + "\"]"));
        verifyRegistration("%FE%AF%0D%0D%B5%1D%C7%23%B78%FD%8B%89%5C%95%3A%5D%22%BA%7B", "feaf0d0db51dc723b738fd8b895c953a5d22ba7b");
    }

    @Test
    public void disconectPeer() throws UnsupportedEncodingException {
        assertThat(getInfoHashes(), is("[]"));
        String hexInfoHash = randomInfoHash();
        verifyRegistration(hexInfoHash);
        restTemplate.getForEntity(URI.create("/announce?port=2048&info_hash=" + hexInfoHash + "&event=stopped"), String.class);
        assertThat(getInfoHashes(), is("[]"));
    }

    @Test
    public void shouldConnect() throws Exception {
        String connectionId = startNewConnection();
        // Should get new connectionId every time
        assertThat(connectionId, is(not(startNewConnection())));
    }

    @Test
    @Ignore("Pending implementation")
    public void shouldAnnounceOverUdpv6() throws Exception {
        String infohash = randomInfoHash();
        String transactionId = "b77e0246";
        String payload = buildPayload(infohash, transactionId);

        byte[] buf = HexUtils.fromHexString(payload);
        InetAddress address = InetAddress.getLoopbackAddress();
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        DatagramSocket socket = new DatagramSocket();

        InetAddress loopbackAddress = Inet6Address.getByName("::1");
        packet.setAddress(loopbackAddress);
        packet.setPort(8090);
        socket.send(packet);

        assertThat(getInfoHashes(), is("[\"" + infohash + "\"]"));

        DatagramPacket responsePacket = new DatagramPacket(new byte[26], 26);
        socket.receive(responsePacket);
        //TODO assert response for ipv6
    }

    @Test
    public void shoudAnnounceOverUddp() throws Exception {
        String infohash = randomInfoHash();
        Peer anotherPeer = new Peer();
        String anotherIp = "ffffffff";
        anotherPeer.setIp(anotherIp);
        anotherPeer.setPort(1);
        anotherPeer.setExpires(new Date(System.currentTimeMillis() + 10000));
        anotherPeer.setInfoHash(infohash);
        peerRepository.save(anotherPeer);

        String transactionId = "b77e0246";
        String payload = buildPayload(infohash, transactionId);

        byte[] buf = HexUtils.fromHexString(payload);
        InetAddress address = InetAddress.getLoopbackAddress();
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        DatagramSocket socket = new DatagramSocket();
        packet.setAddress(address);
        packet.setPort(8090);
        socket.send(packet);

        assertThat(getInfoHashes(), is("[\"" + infohash + "\"]"));
        DatagramPacket responsePacket = new DatagramPacket(new byte[26], 26);
        socket.receive(responsePacket);

        String interval = "00000e10";
        String expected = "00000001" + transactionId + interval + "00000001" + "00000001" + "ffffffff0001";
        assertThat(HexUtils.toHexString(responsePacket.getData()), is(expected));
    }

    @NotNull
    private String buildPayload(String infohash, String transactionId) {
        String connectionId = "f56350d9cf7c3735";
        String action = "00000001";
        String port = "2327";
        String event = "00000002";
        String peerId = "2d7142333347302d684e545f6b59746865214e5a";
        return connectionId + action + transactionId + infohash + peerId + "000000000019f24700000000050400000000000000000000" + event + "00000000fcd454b2000000c8" + port + "02092f616e6e6f756e6365";
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
        restTemplate.getForEntity(URI.create("/announce?port=2048&info_hash=" + rawInfoHash), String.class);
        String body = getInfoHashes();
        assertThat(body, Matchers.containsString(readableInfoHash));
    }

    private String getInfoHashes() {
        return restTemplate.getForEntity(URI.create("/info_hash"), String.class).getBody();
    }

    @NotNull
    private String randomInfoHash() {
        byte[] bytes = new byte[20];
        new Random().nextBytes(bytes);
        return HexUtils.toHexString(bytes);
    }
}
