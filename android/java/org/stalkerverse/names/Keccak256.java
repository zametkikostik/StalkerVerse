package org.stalkerverse.names;

/**
 * Keccak-256 (Ethereum / ENS, not NIST SHA-3). Verified against ENS namehash vectors:
 * labelhash("eth"), namehash("eth"), namehash("foo.eth"), namehash("alice.eth").
 */
public final class Keccak256 {
  private static final long[] RC = {
    0x0000000000000001L, 0x0000000000008082L, 0x800000000000808aL, 0x8000000080008000L,
    0x000000000000808bL, 0x0000000080000001L, 0x8000000080008081L, 0x8000000000008009L,
    0x000000000000008aL, 0x0000000000000088L, 0x0000000080008009L, 0x000000008000000aL,
    0x000000008000808bL, 0x800000000000008bL, 0x8000000000008089L, 0x8000000000008003L,
    0x8000000000008002L, 0x8000000000000080L, 0x000000000000800aL, 0x800000008000000aL,
    0x8000000080008081L, 0x8000000000008080L, 0x0000000080000001L, 0x8000000080008008L
  };

  private static final int[][] ROT = {
    {0, 36, 3, 41, 18},
    {1, 44, 10, 45, 2},
    {62, 6, 43, 15, 61},
    {28, 55, 25, 21, 56},
    {27, 20, 39, 8, 14}
  };

  private Keccak256() {}

  public static byte[] hash(byte[] input) {
    long[] st = new long[25];
    int rate = 136;
    byte[] pad = new byte[input.length + rate];
    System.arraycopy(input, 0, pad, 0, input.length);
    int len = input.length;
    pad[len++] = 0x01;
    while (len % rate != 0) pad[len++] = 0x00;
    pad[len - 1] |= (byte) 0x80;
    for (int i = 0; i < len; i += rate) {
      for (int j = 0; j < rate; j += 8) {
        long v = 0;
        for (int k = 0; k < 8; k++) {
          v |= ((long) pad[i + j + k] & 0xffL) << (8 * k);
        }
        st[j / 8] ^= v;
      }
      keccakF(st);
    }
    byte[] out = new byte[32];
    for (int i = 0; i < 4; i++) {
      long v = st[i];
      for (int j = 0; j < 8; j++) out[i * 8 + j] = (byte) (v >>> (8 * j));
    }
    return out;
  }

  public static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
    return sb.toString();
  }

  private static long rotl(long x, int n) {
    n %= 64;
    return (x << n) | (x >>> (64 - n));
  }

  private static void keccakF(long[] st) {
    long[] c = new long[5];
    long[] d = new long[5];
    long[] b = new long[25];
    for (int round = 0; round < 24; round++) {
      for (int x = 0; x < 5; x++) {
        c[x] = st[x] ^ st[x + 5] ^ st[x + 10] ^ st[x + 15] ^ st[x + 20];
      }
      for (int x = 0; x < 5; x++) {
        d[x] = c[(x + 4) % 5] ^ rotl(c[(x + 1) % 5], 1);
      }
      for (int i = 0; i < 25; i++) st[i] ^= d[i % 5];
      for (int x = 0; x < 5; x++) {
        for (int y = 0; y < 5; y++) {
          b[y + ((2 * x + 3 * y) % 5) * 5] = rotl(st[x + 5 * y], ROT[x][y]);
        }
      }
      for (int x = 0; x < 5; x++) {
        for (int y = 0; y < 5; y++) {
          st[x + 5 * y] = b[x + 5 * y] ^ (~b[((x + 1) % 5) + 5 * y] & b[((x + 2) % 5) + 5 * y]);
        }
      }
      st[0] ^= RC[round];
    }
  }
}
