/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.trofo.seeder.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andoreh
 */
public class Hash {

    private static final String HEXES = "0123456789ABCDEF";

    public static String gerarHash(byte[] conteudo) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            byte[] digest = instance.digest(conteudo);
            return getHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Hash.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String gerarHash(String string) {
        return gerarHash(string.getBytes());
    }

    public static String getHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static char[] hexStringToByteArray(String s) {
        int len = s.length();
        char[] data = new char[len / 2];
        for (int i = 0; i < len; i = i + 2) {
            data[i / 2] = (char) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
