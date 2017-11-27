package br.com.trofo.seeder

import br.com.trofo.seeder.PeerService.INTERVAL
import br.com.trofo.seeder.dao.PeerDao
import br.com.trofo.seeder.dao.PeerRepository
import br.com.trofo.seeder.entity.Peer
import br.com.trofo.seeder.util.Bencode
import br.com.trofo.seeder.util.BitTorrentEncoder
import org.apache.tomcat.util.buf.HexUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
class AnnounceController {

    val logger = LoggerFactory.getLogger(AnnounceController::class.java)

    @Value("\${spring.http.encoding.charset}")
    private lateinit var encoding: String

    @Autowired
    private lateinit var peerDao: PeerDao

    @Autowired
    private lateinit var peerRepository: PeerRepository

    @RequestMapping("/info_hash")
    fun infoHashes() : Set<String> {
        logger.debug("List of infos requested")
        return peerRepository.findDistinctInfoHashes()
    }

    @RequestMapping("/announce", produces = arrayOf("text/plain"))
    fun announce(
            @RequestParam(value = "info_hash") infoHash: String,
            @RequestParam(value = "event", required = false) eventType: String? = "started",
            request: HttpServletRequest): String {

        var hexString = BitTorrentEncoder.toHexString(infoHash, Charset.forName(encoding)).toLowerCase()

        var responseString = "error"
        try {
            val requestingPeer = buildRequestingPeer(eventType ?: "started", hexString, request)

            val peers = peerDao.getPeers(requestingPeer, 100)

            responseString = buildCompactResponse(peers)

            persistOrUpdatePeer(requestingPeer)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return responseString


    }

    @Throws(NumberFormatException::class)
    private fun getPort(request: HttpServletRequest): Int {
        val parameter = request.getParameter("port")
        return if (parameter != null) {
            Integer.parseInt(parameter)
        } else request.remotePort
    }

    @Throws(BeansException::class)
    private fun persistOrUpdatePeer(requestingPeer: Peer) {
        peerDao.persistEntity(requestingPeer)
    }

    @Throws(Exception::class)
    private fun buildCompactResponse(peers: Collection<Peer>): String {
        val resultIpV6 = StringBuilder()
        val resultIpV4 = StringBuilder()
        var seeders = 0
        var leechers = 0
        for (requestingPeer in peers) {

            if (requestingPeer.isComplete) {
                seeders++
            } else {
                leechers++
            }

            val ipBytes = BitTorrentEncoder.hexStringToByteArray(requestingPeer.ip)

            val port = ByteBuffer.allocate(4)
            port.putInt(requestingPeer.port!!)
            if (requestingPeer.ip.length == 8) {
                resultIpV4.append(ipBytes)
                resultIpV4.append(port.get(2).toChar())
                resultIpV4.append(port.get(3).toChar())
            } else {
                resultIpV6.append(ipBytes)
                resultIpV6.append(port.get(2).toChar())
                resultIpV6.append(port.get(3).toChar())
            }

        }

        val responseParams = HashMap<String, String>()
        responseParams.put("complete", seeders.toString())
        responseParams.put("incomplete", leechers.toString())
        responseParams.put("interval", INTERVAL.toString())
        responseParams.put("peers", resultIpV4.toString())
        if (resultIpV6.length > 0) {
            responseParams.put("peers_ipv6", resultIpV6.toString())
        }

        return Bencode.encode(responseParams)
    }

    //TODO replace this by the one in PeerService
    @Throws(NumberFormatException::class, UnknownHostException::class)
    private fun buildRequestingPeer(event: String, infoHash: String, request: HttpServletRequest): Peer {
        val requestingPeer = Peer()
        requestingPeer.infoHash = infoHash
        requestingPeer.port = getPort(request)

        val remoteAddress = getAddress(request)
        requestingPeer.ip = HexUtils.toHexString(remoteAddress.address)

        val expireTime: Date
        if (event == "stopped") {
            expireTime = Date()
        } else {
            expireTime = Date(System.currentTimeMillis() + (INTERVAL.toDouble() * 1000.0 * 1.2).toInt())
        }
        requestingPeer.expires = expireTime

        if (event == "completed") {
            requestingPeer.isComplete = true
        }

        return requestingPeer
    }

    @Throws(UnknownHostException::class)
    private fun getAddress(request: HttpServletRequest): InetAddress {
        var remoteAddress = InetAddress.getByName(getNginxIp(request))
        // check for ip
        if (request.parameterMap.containsKey("ip")) {
            // is this on the local LAN?
            if (remoteAddress.isSiteLocalAddress) {
                // honour the ip setting
                remoteAddress = InetAddress.getByName(request.getParameter("ip"))
            }
        }
        return remoteAddress
    }


    private fun getNginxIp(request: HttpServletRequest): String {
        val realIP = request.getHeader("X-Real-IP")
        return realIP ?: request.remoteAddr
    }
}
