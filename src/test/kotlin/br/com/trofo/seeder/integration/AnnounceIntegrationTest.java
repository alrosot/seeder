package br.com.trofo.seeder.integration;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.net.URI;

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
