package org.example.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HashUtilsTest {

    @Test
    @DisplayName("sha256 should compute correct hash for known string")
    void testSha256KnownVector() {
        byte[] emptyHash = HashUtils.sha256("".getBytes(StandardCharsets.UTF_8));
        String emptyHex = HashUtils.bytesToHex(emptyHash);
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", emptyHex);

        byte[] helloHash = HashUtils.sha256("hello".getBytes(StandardCharsets.UTF_8));
        String helloHex = HashUtils.bytesToHex(helloHash);
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", helloHex);
    }

    @Test
    @DisplayName("sha256 should throw IllegalArgumentException for null input")
    void testSha256NullInput() {
        assertThrows(IllegalArgumentException.class, () -> HashUtils.sha256(null));
    }

    @Test
    @DisplayName("concatBytes should correctly combine two byte arrays")
    void testConcatBytes() {
        byte[] a = new byte[]{1, 2, 3};
        byte[] b = new byte[]{4, 5};
        byte[] combined = HashUtils.concatBytes(a, b);

        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, combined);
    }

    @Test
    @DisplayName("concatBytes should throw IllegalArgumentException when either argument is null")
    void testConcatBytesNullArgs() {
        assertThrows(IllegalArgumentException.class, () -> HashUtils.concatBytes(null, new byte[]{1}));
        assertThrows(IllegalArgumentException.class, () -> HashUtils.concatBytes(new byte[]{1}, null));
    }

    @Test
    @DisplayName("bytesToHex should format byte array into lowercase hex string")
    void testBytesToHex() {
        byte[] bytes = new byte[]{(byte) 0x00, (byte) 0x0F, (byte) 0x10, (byte) 0xAF, (byte) 0xFF};
        assertEquals("000f10afff", HashUtils.bytesToHex(bytes));
        assertEquals("", HashUtils.bytesToHex(null));
        assertEquals("", HashUtils.bytesToHex(new byte[0]));
    }
}
