package br.com.trofo.seeder.util;

import org.apache.tomcat.util.buf.HexUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;

public class BitTorrentEncoder {

    @NotNull
    public static String toHexString(@NotNull String bytesAsString, @Nullable Charset charset) {
        if(bytesAsString.length()==40) return bytesAsString;
        try {
            return HexUtils.toHexString(bytesAsString.getBytes(charset));
        } catch (Exception e) {
            return "error: " + e;
        }
    }
}
