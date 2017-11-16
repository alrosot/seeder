package br.com.trofo.seeder.util;

import org.apache.tomcat.util.buf.HexUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;

public class BitTorrentEncoder {

    public static char[] hexStringToByteArray(String s) {
        int len = s.length();
        char[] data = new char[len / 2];
        for (int i = 0; i < len; i = i + 2) {
            data[i / 2] = (char) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

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
