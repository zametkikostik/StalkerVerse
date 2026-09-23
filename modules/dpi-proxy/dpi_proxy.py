#!/usr/bin/env python3
"""DPI proxy: HTTP CONNECT + SOCKS5 + ClientHello fragmentation."""
from __future__ import annotations
import argparse, random, socket, struct, threading, time
from typing import List, Optional, Tuple

DEFAULT_HOST, DEFAULT_HTTP_PORT, DEFAULT_SOCKS_PORT, BUFFER = '127.0.0.1', 8080, 1080, 65536
FRAGMENT_DELAY = (0.005, 0.025)

def find_sni(data: bytes) -> Optional[Tuple[int, int, str]]:
    try:
        if len(data) < 50 or data[0] != 0x16: return None
        pos = 5
        if data[pos] != 0x01: return None
        pos += 4 + 34
        pos += 1 + data[pos]
        cs_len = struct.unpack('!H', data[pos:pos+2])[0]; pos += 2 + cs_len
        pos += 1 + data[pos]
        ext_len = struct.unpack('!H', data[pos:pos+2])[0]; pos += 2
        end = pos + ext_len
        while pos + 4 <= end:
            et, es = struct.unpack('!HH', data[pos:pos+4]); pos += 4
            if et == 0:
                nl = struct.unpack('!H', data[pos+3:pos+5])[0]
                ns, ne = pos + 5, pos + 5 + nl
                return ns, ne, data[ns:ne].decode('ascii', errors='ignore')
            pos += es
    except Exception:
        pass
    return None

def smart_fragment(data: bytes, mode: str = 'both') -> List[bytes]:
    if mode == 'record' or (mode == 'both' and not find_sni(data)):
        if len(data) < 10 or data[0] != 0x16: return [data]
        try:
            rec_len = struct.unpack('!H', data[3:5])[0]
            payload = data[5:5+rec_len]
        except Exception:
            return [data]
        chunks, offset = [], 0
        while offset < len(payload):
            size = random.randint(18, 42)
            chunk = payload[offset:offset+size]
            chunks.append(bytes([0x16, 0x03, 0x01]) + struct.pack('!H', len(chunk)) + chunk)
            offset += size
        return chunks or [data]
    info = find_sni(data)
    if not info:
        mid = len(data) // 2
        return [data[:mid], data[mid:]] if mid else [data]
    s, e, host = info
    print(f'    [SNI] {host}')
    parts = [data[:s], data[s:e], data[e:]]
    return [p for p in parts if p]

def forward(src, dst, fragment_first=False, mode='both'):
    first = fragment_first
    try:
        while True:
            data = src.recv(BUFFER)
            if not data: break
            if first and data[0:1] == b'\x16':
                parts = smart_fragment(data, mode)
                for i, part in enumerate(parts):
                    dst.sendall(part)
                    if i < len(parts) - 1: time.sleep(random.uniform(*FRAGMENT_DELAY))
                first = False
            else:
                dst.sendall(data); first = False
    except Exception:
        pass

def pipe(client, remote, mode):
    t1 = threading.Thread(target=forward, args=(client, remote, True, mode), daemon=True)
    t2 = threading.Thread(target=forward, args=(remote, client, False, mode), daemon=True)
    t1.start(); t2.start(); t1.join(); t2.join()
    try: remote.close()
    except Exception: pass

def handle_http(client, addr, mode):
    try:
        data = client.recv(BUFFER)
        if not data.startswith(b'CONNECT '):
            client.sendall(b'HTTP/1.1 405\r\n\r\n'); return
        target = data.split(b'\r\n')[0].decode().split()[1]
        host, _, ps = target.partition(':'); port = int(ps) if ps else 443
        print(f'[HTTP] {addr[0]} -> {host}:{port}')
        remote = socket.create_connection((host, port), timeout=12)
        client.sendall(b'HTTP/1.1 200 Connection Established\r\n\r\n')
        pipe(client, remote, mode)
    except Exception as e:
        print(f'[HTTP] error {e}')
    finally:
        try: client.close()
        except Exception: pass

def handle_socks5(client, addr, mode):
    try:
        data = client.recv(256)
        if len(data) < 2 or data[0] != 0x05: return
        client.sendall(b'\x05\x00')
        data = client.recv(BUFFER)
        if len(data) < 7 or data[0] != 0x05 or data[1] != 0x01:
            client.sendall(b'\x05\x07\x00\x01' + b'\x00'*6); return
        atyp = data[3]
        if atyp == 0x01:
            host = socket.inet_ntoa(data[4:8]); port = struct.unpack('!H', data[8:10])[0]
        elif atyp == 0x03:
            dlen = data[4]; host = data[5:5+dlen].decode(); port = struct.unpack('!H', data[5+dlen:7+dlen])[0]
        else:
            client.sendall(b'\x05\x08\x00\x01' + b'\x00'*6); return
        print(f'[SOCKS5] {addr[0]} -> {host}:{port}')
        try:
            remote = socket.create_connection((host, port), timeout=12)
        except Exception:
            client.sendall(b'\x05\x05\x00\x01' + b'\x00'*6); return
        client.sendall(b'\x05\x00\x00\x01' + b'\x00'*6)
        pipe(client, remote, mode)
    except Exception as e:
        print(f'[SOCKS5] error {e}')
    finally:
        try: client.close()
        except Exception: pass

def serve(host, port, handler, mode, name):
    sock = socket.socket(); sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((host, port)); sock.listen(128)
    print(f'[*] {name} on {host}:{port} fragment={mode}')
    while True:
        try:
            c, a = sock.accept()
            threading.Thread(target=handler, args=(c, a, mode), daemon=True).start()
        except KeyboardInterrupt:
            break

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--host', default=DEFAULT_HOST)
    ap.add_argument('--http-port', type=int, default=DEFAULT_HTTP_PORT)
    ap.add_argument('--socks-port', type=int, default=DEFAULT_SOCKS_PORT)
    ap.add_argument('--mode', choices=['sni','record','both'], default='both')
    ap.add_argument('--http-only', action='store_true')
    ap.add_argument('--socks-only', action='store_true')
    args = ap.parse_args()
    if not args.socks_only:
        threading.Thread(target=serve, args=(args.host, args.http_port, handle_http, args.mode, 'HTTP'), daemon=True).start()
    if not args.http_only:
        threading.Thread(target=serve, args=(args.host, args.socks_port, handle_socks5, args.mode, 'SOCKS5'), daemon=True).start()
    try:
        while True: time.sleep(1)
    except KeyboardInterrupt:
        print('stopped')

if __name__ == '__main__':
    main()
