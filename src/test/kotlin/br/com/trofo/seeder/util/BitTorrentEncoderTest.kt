package br.com.trofo.seeder.util

import br.com.trofo.seeder.EncoderUtil.byteArrayToURLString
import org.apache.tomcat.util.buf.HexUtils
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Test
import java.net.URLDecoder
import java.nio.charset.Charset

class BitTorrentEncoderTest {

    @Test
    fun `should convert info hash`() {
        val expected = "feaf0d0db51dc723b738fd8b895c953a5d22ba7b"
        val encoding = "windows-1252"

        val simulatedEncoded = URLDecoder.decode(byteArrayToURLString(HexUtils.fromHexString(expected)), encoding)

        val toByteArray = simulatedEncoded.toByteArray(Charset.forName(encoding))
        assertThat(HexUtils.toHexString(toByteArray), CoreMatchers.`is`(expected))

    }

}