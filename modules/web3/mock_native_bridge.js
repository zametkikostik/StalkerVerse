(function () {
  'use strict';
  if (window.__WEB3_NATIVE_BRIDGE__) return;
  const MOCK_ADDRESS = '0x742d35Cc6634C0532925a3b844Bc454e4438f44e';
  let currentChainId = '0x1';
  let accounts = [];
  function respond(id, result, error = null) {
    const msg = { type: 'WEB3_RESPONSE', id, result, error };
    window.postMessage(msg, '*');
    if (typeof window.__WEB3_RECEIVE__ === 'function') window.__WEB3_RECEIVE__(msg);
  }
  const bridge = {
    postMessage(jsonStr) {
      let data;
      try { data = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr; } catch (e) { return; }
      if (data.type !== 'WEB3_REQUEST') return;
      const { id, method, params = [] } = data;
      setTimeout(() => {
        try {
          switch (method) {
            case 'eth_requestAccounts':
            case 'eth_accounts':
              if (!accounts.length) accounts = [MOCK_ADDRESS];
              respond(id, accounts); break;
            case 'eth_chainId': respond(id, currentChainId); break;
            case 'wallet_switchEthereumChain':
              currentChainId = params[0]?.chainId || currentChainId;
              respond(id, null); break;
            case 'personal_sign':
            case 'eth_sign':
            case 'eth_signTypedData_v4':
              respond(id, '0x' + 'ab'.repeat(65)); break;
            case 'eth_sendTransaction':
              respond(id, '0x' + 'deadbeef'.repeat(8)); break;
            default: respond(id, null);
          }
        } catch (err) { respond(id, null, { message: err.message }); }
      }, 80);
    }
  };
  window.__WEB3_NATIVE_BRIDGE__ = bridge;
  window.Web3Bridge = bridge;
})();
