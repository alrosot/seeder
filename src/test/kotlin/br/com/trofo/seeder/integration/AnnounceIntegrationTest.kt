package br.com.trofo.seeder.integration

import br.com.trofo.seeder.dao.PeerRepository
import br.com.trofo.seeder.entity.Peer
import org.apache.tomcat.util.buf.HexUtils
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit4.SpringRunner
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.*
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnnounceIntegrationTest {

    @Autowired
    private val restTemplate: TestRestTemplate? = null

    @Autowired
    private val peerRepository: PeerRepository? = null

    private val infoHashes: String
        get() = restTemplate!!.getForEntity(URI.create("/info_hash"), String::class.java).body

    @Before
    fun cleanUp() {
        peerRepository!!.deleteAll()
    }

    @Test
    @Throws(UnsupportedEncodingException::class)
    fun announceNewTorrent() {
        assertThat(infoHashes, `is`("[]"))
        val hexInfoHash = randomInfoHash()
        verifyRegistration(hexInfoHash)
        verifyRegistration(hexInfoHash) // should not duplicate
        assertThat(infoHashes, `is`("[\"$hexInfoHash\"]"))
        verifyRegistration("%FE%AF%0D%0D%B5%1D%C7%23%B78%FD%8B%89%5C%95%3A%5D%22%BA%7B", "feaf0d0db51dc723b738fd8b895c953a5d22ba7b")
    }

    @Test
    @Throws(UnsupportedEncodingException::class)
    fun disconectPeer() {
        assertThat(infoHashes, `is`("[]"))
        val hexInfoHash = randomInfoHash()
        verifyRegistration(hexInfoHash)
        restTemplate!!.getForEntity(URI.create("/announce?port=2048&info_hash=$hexInfoHash&event=stopped"), String::class.java)
        assertThat(infoHashes, `is`("[]"))
    }

    @Test
    @Throws(Exception::class)
    fun shouldConnect() {
        val connectionId = startNewConnection()
        // Should get new connectionId every time
        assertThat(connectionId, `is`(not(startNewConnection())))
    }

    @Test
    @Throws(Exception::class)
    fun shouldAnnounceOverUdpIPv6() {
        val address = Inet6Address.getByName("::1")
        val anotherIp = HexUtils.toHexString(Inet6Address.getByName("::234").address)

        announceAndVerifyResponse(anotherIp, address)
    }

    @Test
    @Throws(Exception::class)
    fun shoudAnnounceOverUddpIPv4() {
        val anotherIp = "ffffffff"
        val address = InetAddress.getLoopbackAddress()

        announceAndVerifyResponse(anotherIp, address)
    }

    @Throws(IOException::class)
    private fun announceAndVerifyResponse(anotherIp: String?, address: InetAddress) {
        val infohash = randomInfoHash()
        val anotherPeer = Peer()
        anotherPeer.ip = anotherIp
        anotherPeer.port = 1
        anotherPeer.expires = Date(System.currentTimeMillis() + 10000)
        anotherPeer.infoHash = infohash
        peerRepository!!.save(anotherPeer)

        val transactionId = "b77e0246"
        val payload = buildPayload(infohash, transactionId)

        val socket = sendPayload(address, payload)

        assertThat(infoHashes, `is`("[\"$infohash\"]"))

        val ipv6 = anotherIp!!.length > 8

        val buff = if (ipv6) ByteArray(38) else ByteArray(26)

        val responsePacket = DatagramPacket(buff, buff.size)
        socket.receive(responsePacket)

        val interval = "00000e10"
        val expected = "00000001" + transactionId + interval + "00000001" + "00000001" + anotherIp + "0001"
        assertThat(HexUtils.toHexString(responsePacket.data), `is`(expected))
    }

    @Throws(IOException::class)
    private fun sendPayload(address: InetAddress, payload: String): DatagramSocket {
        val buf = HexUtils.fromHexString(payload)
        val packet = DatagramPacket(buf, buf.size)
        val socket = DatagramSocket()
        packet.address = address
        packet.port = 8090
        socket.send(packet)
        return socket
    }

    private fun buildPayload(infohash: String, transactionId: String): String {
        val connectionId = "f56350d9cf7c3735"
        val action = "00000001"
        val port = "2327"
        val event = "00000002"
        val peerId = "2d7142333347302d684e545f6b59746865214e5a"
        return connectionId + action + transactionId + infohash + peerId + "000000000019f24700000000050400000000000000000000" + event + "00000000fcd454b2000000c8" + port + "02092f616e6e6f756e6365"
    }

    @Throws(IOException::class)
    private fun startNewConnection(): String {
        val buf = HexUtils.fromHexString("000004172710198000000000abcddcba")
        assertThat(buf.size, `is`(16))
        val address = InetAddress.getLoopbackAddress()
        val packet = DatagramPacket(buf, buf.size)
        val socket = DatagramSocket()
        packet.address = address
        packet.port = 8090
        socket.send(packet)

        socket.receive(packet)

        val hexString = HexUtils.toHexString(packet.data)
        assertThat(hexString, Matchers.startsWith("00000000abcddcba"))
        val connectionId = hexString.substring(16)
        assertThat(connectionId.length, `is`(16))
        return connectionId
    }

    private fun verifyRegistration(infoHash: String) {
        verifyRegistration(infoHash, infoHash)
    }

    private fun verifyRegistration(rawInfoHash: String, readableInfoHash: String) {
        restTemplate!!.getForEntity(URI.create("/announce?port=2048&info_hash=" + rawInfoHash), String::class.java)
        val body = infoHashes
        assertThat(body, Matchers.containsString(readableInfoHash))
    }

    private fun randomInfoHash(): String {
        val bytes = ByteArray(20)
        Random().nextBytes(bytes)
        return HexUtils.toHexString(bytes)
    }
}
