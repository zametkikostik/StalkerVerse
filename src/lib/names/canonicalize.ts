export function canonicalizeHostname(input: string): string | null {
  let s = input.trim().toLowerCase();
  if (!s) return null;
  try {
    if (s.includes("://")) {
      const u = new URL(s);
      s = u.hostname;
    }
  } catch {
    /* treat as bare name */
  }
  s = s.replace(/\.$/, "");
  if (s.startsWith("www.")) s = s.slice(4);
  if (!/^[a-z0-9._-]+$/.test(s) && !s.includes("xn--")) {
    if (!/^[\p{L}\p{N}._-]+$/u.test(s)) return null;
  }
  if (s.length > 253) return null;
  return s;
}

export function hostnameTld(hostname: string): string {
  const parts = hostname.split(".");
  return parts.length >= 2 ? parts[parts.length - 1]! : "";
}

export function isPunycodeTld(tld: string): boolean {
  return tld.startsWith("xn--");
}
