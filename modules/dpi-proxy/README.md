# DPI Protection Proxy (v2)

HTTP CONNECT :8080 and SOCKS5 :1080 with ClientHello fragmentation.

```bash
python3 dpi_proxy.py
python3 dpi_proxy.py --socks-only --mode sni
```

Chromium: `--proxy-server=socks5://127.0.0.1:1080`
Android: `DpiProxyService.start(context)`
