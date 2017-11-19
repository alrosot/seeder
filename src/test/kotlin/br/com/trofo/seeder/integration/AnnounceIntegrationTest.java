package br.com.trofo.seeder.integration;

import org.apache.tomcat.util.buf.HexUtils;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
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
    public void shouldAnnounceOverUdp() throws Exception {
        String connectionId = startNewConnection();
        // Should get new connectionId every time
        assertThat(connectionId, is(not(startNewConnection())));
    }

    @NotNull
    private String startNewConnection() throws IOException {
        byte[] buf = HexUtils.fromHexString("000004172710198000000000abcddcba");
        assertThat(buf.length, is(16));
        System.out.println("buf: " + buf);
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
