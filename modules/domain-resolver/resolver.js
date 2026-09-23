(function (global) {
  'use strict';
  const WEB3_TLDS = new Set(['eth','crypto','nft','wallet','blockchain','dao','888','zil','bitcoin','coin']);
  const CONFIG = {
    udResolutionApi: 'https://api.unstoppabledomains.com/resolve/domains/',
    ipfsGateways: ['https://ipfs.io/ipfs/', 'https://cloudflare-ipfs.com/ipfs/'],
    arweaveGateway: 'https://arweave.net/',
    cacheTtlMs: 5 * 60 * 1000
  };
  const cache = new Map();
  function isWeb3Domain(hostname) {
    if (!hostname) return false;
    const parts = hostname.toLowerCase().replace(/\.$/, '').split('.');
    return parts.length >= 2 && WEB3_TLDS.has(parts[parts.length - 1]);
  }
  async function resolveENS(name) {
    const clean = name.toLowerCase().replace(/\.eth$/, '') + '.eth';
    const limoUrl = 'https://' + clean + '.limo';
    try {
      const head = await fetch(limoUrl, { method: 'HEAD', redirect: 'follow', signal: AbortSignal.timeout(4000) });
      if (head.ok || head.status === 301 || head.status === 302) return { url: limoUrl, type: 'ens-limo' };
    } catch (_) {}
    try {
      const res = await fetch('https://api.ensdata.net/' + clean, { signal: AbortSignal.timeout(5000) });
      if (res.ok) {
        const data = await res.json();
        const ch = data.contenthash || data.content;
        if (typeof ch === 'string' && ch.startsWith('ipfs://'))
          return { url: CONFIG.ipfsGateways[0] + ch.slice(7), type: 'ens-ipfs' };
        if (data.url) return { url: data.url, type: 'ens-url' };
      }
    } catch (_) {}
    return { url: limoUrl, type: 'ens-limo-fallback' };
  }
  async function resolveUnstoppable(name) {
    try {
      const res = await fetch(CONFIG.udResolutionApi + encodeURIComponent(name.toLowerCase()), {
        headers: { Accept: 'application/json' }, signal: AbortSignal.timeout(6000)
      });
      if (res.ok) {
        const data = await res.json();
        const records = data.records || {};
        const ipfs = records['ipfs.html.value'] || records.ipfs;
        if (ipfs) return { url: CONFIG.ipfsGateways[0] + String(ipfs).replace(/^ipfs:\/\//, ''), type: 'ud-ipfs' };
      }
    } catch (_) {}
    return { url: 'https://' + name.toLowerCase() + '.unstoppabledomains.com', type: 'ud-gateway' };
  }
  async function resolve(input) {
    if (!input) return null;
    const trimmed = input.trim();
    if (trimmed.startsWith('ipfs://')) return { url: CONFIG.ipfsGateways[0] + trimmed.slice(7), type: 'ipfs' };
    if (trimmed.startsWith('ar://')) return { url: CONFIG.arweaveGateway + trimmed.slice(5), type: 'arweave' };
    let hostname = trimmed, path = '/';
    try {
      if (trimmed.includes('://')) { const u = new URL(trimmed); hostname = u.hostname; path = u.pathname + u.search; }
      else if (trimmed.includes('/')) { const i = trimmed.indexOf('/'); hostname = trimmed.slice(0, i); path = trimmed.slice(i); }
    } catch (_) {}
    hostname = hostname.toLowerCase().replace(/\.$/, '');
    if (!isWeb3Domain(hostname)) return null;
    const cached = cache.get(hostname);
    if (cached && cached.expires > Date.now()) return Object.assign({}, cached.value, { url: cached.value.url.replace(/\/$/, '') + (path === '/' ? '' : path) });
    const tld = hostname.split('.').pop();
    let result = tld === 'eth' ? await resolveENS(hostname) : await resolveUnstoppable(hostname);
    if (result) {
      cache.set(hostname, { value: result, expires: Date.now() + CONFIG.cacheTtlMs });
      if (path && path !== '/') result = Object.assign({}, result, { url: result.url.replace(/\/$/, '') + path });
    }
    return result;
  }
  const api = { isWeb3Domain, resolve, WEB3_TLDS: [...WEB3_TLDS], CONFIG, clearCache() { cache.clear(); } };
  if (typeof module !== 'undefined') module.exports = api;
  if (typeof window !== 'undefined') window.__WEB3_DOMAIN_RESOLVER__ = api;
})(typeof globalThis !== 'undefined' ? globalThis : this);
