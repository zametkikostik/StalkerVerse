package org.stalkerverse.names;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** ENS namehash (EIP-137). Pure local — no network. */
public final class EnsNamehash {
  private EnsNamehash() {}

  public static byte[] namehash(String name) {
    byte[] node = new byte[32];
    if (name == null || name.isEmpty()) return node;
    String n = name.trim().toLowerCase(Locale.ROOT);
    if (n.endsWith(".")) n = n.substring(0, n.length() - 1);
    if (n.isEmpty()) return node;
    String[] labels = n.split("\\.");
    for (int i = labels.length - 1; i >= 0; i--) {
      String label = labels[i];
      if (label.isEmpty()) continue;
      byte[] labelHash = Keccak256.hash(label.getBytes(StandardCharsets.UTF_8));
      byte[] concat = new byte[64];
      System.arraycopy(node, 0, concat, 0, 32);
      System.arraycopy(labelHash, 0, concat, 32, 32);
      node = Keccak256.hash(concat);
    }
    return node;
  }

  public static String namehashHex(String name) {
    return "0x" + Keccak256.toHex(namehash(name));
  }
}
