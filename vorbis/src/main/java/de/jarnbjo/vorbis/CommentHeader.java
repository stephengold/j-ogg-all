/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: CommentHeader.java,v 1.2 2003/03/16 01:11:12 jarnbjo Exp $
 * -----------------------------------------------------------
 *
 * $Author: jarnbjo $
 *
 * Description:
 *
 * Copyright 2002-2003 Tor-Einar Jarnbjo
 * -----------------------------------------------------------
 *
 * Change History
 * -----------------------------------------------------------
 * $Log: CommentHeader.java,v $
 * Revision 1.2  2003/03/16 01:11:12  jarnbjo
 * no message
 */
package de.jarnbjo.vorbis;

import de.jarnbjo.util.io.BitInputStream;
import de.jarnbjo.util.io.ByteArrayBitInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

/**
 */
public class CommentHeader {
    private static final String defaultCharsetName;

    static {
        Charset charset = Charset.forName("ISO-8859-1");
        defaultCharsetName = charset.name();
    }

    public static final String TITLE = "TITLE";
    public static final String ARTIST = "ARTIST";
    public static final String ALBUM = "ALBUM";
    public static final String TRACKNUMBER = "TRACKNUMBER";
    public static final String VERSION = "VERSION";
    public static final String PERFORMER = "PERFORMER";
    public static final String COPYRIGHT = "COPYRIGHT";
    public static final String LICENSE = "LICENSE";
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String GENRE = "GENRE";
    public static final String DATE = "DATE";
    public static final String LOCATION = "LOCATION";
    public static final String CONTACT = "CONTACT";
    public static final String ISRC = "ISRC";
    public static final String METADATA_BLOCK_PICTURE
            = "METADATA_BLOCK_PICTURE";

    final private String vendor;
    final private HashMap<String, ArrayList<String>> comments = new HashMap<>();
    private boolean framingBit;

    private static final long HEADER = 0x736962726f76L; // 'vorbis'

    public CommentHeader(BitInputStream source) throws IOException {
        if (source.getLong(48) != HEADER) {
            throw new VorbisFormatException(
                    "The identification header has an illegal leading.");
        }

        vendor = getString(source);

        int ucLength = source.getInt(32);

        for (int i = 0; i < ucLength; i++) {
            String comment = getString(source);
            int ix = comment.indexOf('=');
            String key = comment.substring(0, ix);
            String value = comment.substring(ix + 1);
            //comments.put(key, value);
            addComment(key, value);
        }

        framingBit = source.getInt(8) != 0;
    }

    public static String byteBufferToString(
            byte[] bytes, int offset, int length)
            throws UnsupportedEncodingException {
        return byteBufferToString(bytes, offset, length, defaultCharsetName);
    }

    public static String byteBufferToString(
            byte[] bytes, int offset, int length, String charsetName)
            throws UnsupportedEncodingException {
        if (length < 1) {
            return "";
        }

        return new String(bytes, offset, length, charsetName);
    }

    private void addComment(String key, String value) {
        ArrayList<String> al = comments.get(key);
        if (al == null) {
            al = new ArrayList<>();
            comments.put(key, al);
        }
        al.add(value);
    }

    public String getVendor() {
        return vendor;
    }

    public String getComment(String key) {
        ArrayList<String> al = comments.get(key);
        return al == null ? null : al.get(0);
    }

    public String[] getComments(String key) {
        ArrayList<String> al = comments.get(key);
        return al == null ? new String[0] : al.toArray(new String[0]);
    }

    public String getTitle() {
        return getComment(TITLE);
    }

    public String[] getTitles() {
        return getComments(TITLE);
    }

    public String getVersion() {
        return getComment(VERSION);
    }

    public String[] getVersions() {
        return getComments(VERSION);
    }

    public String getAlbum() {
        return getComment(ALBUM);
    }

    public String[] getAlbums() {
        return getComments(ALBUM);
    }

    public String getTrackNumber() {
        return getComment(TRACKNUMBER);
    }

    public String[] getTrackNumbers() {
        return getComments(TRACKNUMBER);
    }

    public String getArtist() {
        return getComment(ARTIST);
    }

    public String[] getArtists() {
        return getComments(ARTIST);
    }

    public String getPerformer() {
        return getComment(PERFORMER);
    }

    public String[] getPerformers() {
        return getComments(PERFORMER);
    }

    public String getCopyright() {
        return getComment(COPYRIGHT);
    }

    public String[] getCopyrights() {
        return getComments(COPYRIGHT);
    }

    public String getLicense() {
        return getComment(LICENSE);
    }

    public String[] getLicenses() {
        return getComments(LICENSE);
    }

    public String getOrganization() {
        return getComment(ORGANIZATION);
    }

    public String[] getOrganizations() {
        return getComments(ORGANIZATION);
    }

    public String getDescription() {
        return getComment(DESCRIPTION);
    }

    public String[] getDescriptions() {
        return getComments(DESCRIPTION);
    }

    public String getGenre() {
        return getComment(GENRE);
    }

    public String[] getGenres() {
        return getComments(GENRE);
    }

    public String getDate() {
        return getComment(DATE);
    }

    public String[] getDates() {
        return getComments(DATE);
    }

    public String getLocation() {
        return getComment(LOCATION);
    }

    public String[] getLocations() {
        return getComments(LOCATION);
    }

    public String getContact() {
        return getComment(CONTACT);
    }

    public String[] getContacts() {
        return getComments(CONTACT);
    }

    public String getIsrc() {
        return getComment(ISRC);
    }

    public String[] getIsrcs() {
        return getComments(ISRC);
    }

    private static String getString(BitInputStream source) throws IOException {
        int length = source.getInt(32);

        byte[] strArray = new byte[length];

        for (int i = 0; i < length; i++) {
            strArray[i] = (byte) source.getInt(8);
        }

        return new String(strArray, "UTF-8");
    }

    public byte[] getAlbumArt() throws IOException {
        String base64 = getComment(METADATA_BLOCK_PICTURE);

        if (base64 == null) {
            return null;
        }

        byte[] bytes = Base64.decode(base64);

        BitInputStream bis = new ByteArrayBitInputStream(bytes);
        bis.setEndian(BitInputStream.BIG_ENDIAN);

        int pictype = bis.getInt(32);
        System.err.print("pictype: " + pictype + ", "
                + Integer.toHexString(pictype) + System.lineSeparator());

        int mimeTypeLength = bis.getInt(32);
        System.err.print("mimetype_len: " + mimeTypeLength + ", "
                + Integer.toHexString(mimeTypeLength) + System.lineSeparator());

        String mimetype
                = byteBufferToString(bytes, bis.position() + 1, mimeTypeLength);
        System.err.print("mimetype: " + mimetype + System.lineSeparator());

        bis.skip(mimetype.length() + 2);

        int descStringByteCount = bis.getInt(32);
        System.err.print("descStringByteCount: " + descStringByteCount + ", "
                + Integer.toHexString(descStringByteCount)
                + System.lineSeparator());

        if (descStringByteCount != 0) {
            String descString = byteBufferToString(
                    bytes, bis.position() + 1, descStringByteCount);
            System.err.print(
                    "descString: " + descString + System.lineSeparator());

            bis.skip(descStringByteCount + 2);
        }

        int width = bis.getInt(32);
        System.err.print("width: " + width + ", "
                + Integer.toHexString(width) + System.lineSeparator());

        int height = bis.getInt(32);
        System.err.print("height: " + height + ", "
                + Integer.toHexString(height) + System.lineSeparator());

        int bpp = bis.getInt(32);
        System.err.print("bpp: " + bpp + ", "
                + Integer.toHexString(bpp) + System.lineSeparator());

        int colorCount = bis.getInt(32);
        System.err.print("color_count: " + colorCount + ", "
                + Integer.toHexString(colorCount) + System.lineSeparator());

        int byteCount = bis.getInt(32);
        System.err.print("byte_count: " + byteCount + ", "
                + Integer.toHexString(byteCount) + System.lineSeparator());

        byte[] data = new byte[byteCount];
        System.arraycopy(bytes, bis.position() + 1, data, 0, data.length);

        return data;
    }

    private static class Base64 {
        private final static char[] ALPHABET = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz0123456789+/").toCharArray();
        private static int[] toInt = new int[128];

        static {
            for (int i = 0; i < ALPHABET.length; i++) {
                toInt[ALPHABET[i]] = i;
            }
        }

        public static byte[] decode(String s) {
            int delta = s.endsWith("==") ? 2 : s.endsWith("=") ? 1 : 0;
            byte[] buffer = new byte[s.length() * 3 / 4 - delta];

            int mask = 0xFF;
            int index = 0;

            for (int i = 0; i < s.length(); i += 4) {

                int c0 = toInt[s.charAt(i)];
                int c1 = toInt[s.charAt(i + 1)];

                buffer[index++] = (byte) (((c0 << 2) | (c1 >> 4)) & mask);

                if (index >= buffer.length) {
                    return buffer;
                }

                int c2 = toInt[s.charAt(i + 2)];
                buffer[index++] = (byte) (((c1 << 4) | (c2 >> 2)) & mask);

                if (index >= buffer.length) {
                    return buffer;
                }

                int c3 = toInt[s.charAt(i + 3)];
                buffer[index++] = (byte) (((c2 << 6) | c3) & mask);
            }

            return buffer;
        }
    }
}
