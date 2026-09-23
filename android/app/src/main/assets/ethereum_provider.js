/** See modules/web3/ethereum_provider.js - same EIP-1193 provider */
(function () {
  'use strict';
  if (window.ethereum && window.ethereum.isWeb3Browser) return;
  const CONFIG = { defaultChainId: '0x1', bridgeTimeoutMs: 30000, mockMode: true,
    mockAddress: '0x742d35Cc6634C0532925a3b844Bc454e4438f44e',
    supportedChains: { '0x1':1,'0x89':1,'0xa':1,'0xa4b1':1,'0x2105':1 } };
  const state = { selectedAddress: null, chainId: CONFIG.defaultChainId, isConnected: false };
  const EVENT_TARGET = new EventTarget();
  const pending = new Map();
  function generateId() { return 'wb_' + Math.random().toString(36).slice(2); }
  function sendToNative(method, params) {
    return new Promise((resolve, reject) => {
      const id = generateId();
      const timer = setTimeout(() => { pending.delete(id); reject(new Error('timeout')); }, CONFIG.bridgeTimeoutMs);
      pending.set(id, { resolve, reject, timer });
      const message = { type: 'WEB3_REQUEST', id, method, params, origin: location.origin };
      if (window.__WEB3_NATIVE_BRIDGE__?.postMessage) { try { window.__WEB3_NATIVE_BRIDGE__.postMessage(JSON.stringify(message)); return; } catch (_) {} }
      if (window.Web3Bridge?.postMessage) { try { window.Web3Bridge.postMessage(JSON.stringify(message)); return; } catch (_) {} }
      try { window.postMessage(message, '*'); } catch (_) {}
      if (CONFIG.mockMode) { clearTimeout(timer); pending.delete(id); handleMock(method, params).then(resolve).catch(reject); }
    });
  }
  function handleNativeResponse(data) {
    if (!data || data.type !== 'WEB3_RESPONSE' || !data.id) return;
    const e = pending.get(data.id); if (!e) return;
    clearTimeout(e.timer); pending.delete(data.id);
    data.error ? e.reject(new Error(data.error.message || data.error)) : e.resolve(data.result);
  }
  window.addEventListener('message', (ev) => { if (ev.data?.type === 'WEB3_RESPONSE') handleNativeResponse(ev.data); });
  window.__WEB3_RECEIVE__ = handleNativeResponse;
  async function handleMock(method, params) {
    if (method === 'eth_requestAccounts' || method === 'eth_accounts') {
      if (!state.selectedAddress) { state.selectedAddress = CONFIG.mockAddress; state.isConnected = true; provider.selectedAddress = state.selectedAddress; emit('accountsChanged', [state.selectedAddress]); emit('connect', { chainId: state.chainId }); }
      return [state.selectedAddress];
    }
    if (method === 'eth_chainId') return state.chainId;
    if (method === 'wallet_switchEthereumChain') {
      const t = params[0]?.chainId; if (!t || !CONFIG.supportedChains[t]) { const e = new Error('chain'); e.code = 4902; throw e; }
      state.chainId = t; provider.chainId = t; emit('chainChanged', t); return null;
    }
    if (method.indexOf('sign') >= 0) return '0x' + 'ab'.repeat(65);
    if (method === 'eth_sendTransaction') return '0x' + 'cd'.repeat(32);
    return null;
  }
  function emit(name, data) { EVENT_TARGET.dispatchEvent(new CustomEvent(name, { detail: data })); }
  const provider = {
    isMetaMask: false, isWeb3Browser: true,
    get chainId() { return state.chainId; }, set chainId(v) { state.chainId = v; },
    get selectedAddress() { return state.selectedAddress; }, set selectedAddress(v) { state.selectedAddress = v; },
    isConnected() { return state.isConnected && !!state.selectedAddress; },
    request({ method, params = [] }) { return sendToNative(method, params); },
    enable() { return this.request({ method: 'eth_requestAccounts' }); },
    send(m, p) { return typeof m === 'string' ? this.request({ method: m, params: p || [] }) : this.request(m); },
    sendAsync(payload, cb) { this.request(payload).then(r => cb(null, { id: payload.id, jsonrpc: '2.0', result: r })).catch(e => cb(e)); },
    on(ev, fn) { EVENT_TARGET.addEventListener(ev, (e) => fn(e.detail !== undefined ? e.detail : e)); },
    removeListener(ev, fn) { EVENT_TARGET.removeEventListener(ev, fn); },
    _metamask: { isUnlocked: () => Promise.resolve(true) }
  };
  try { Object.defineProperty(window, 'ethereum', { value: provider, writable: false, configurable: false }); }
  catch (_) { window.ethereum = provider; }
  window.web3 = window.web3 || { currentProvider: provider };
  console.info('[Web3Browser] provider injected');
})();
