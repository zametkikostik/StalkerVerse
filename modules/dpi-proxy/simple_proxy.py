#!/usr/bin/env python3
"""Simple HTTP CONNECT proxy with basic ClientHello fragmentation. Prefer dpi_proxy.py."""
import socket, threading, time
LISTEN_HOST, LISTEN_PORT, BUFFER = '127.0.0.1', 8080, 65536

def fragment(data):
    if len(data) < 50 or data[0] != 0x16: return [data]
    mid = len(data) // 3
    return [data[:mid], data[mid:2*mid], data[2*mid:]]

def forward(src, dst, frag=False):
    first = frag
    try:
        while True:
            data = src.recv(BUFFER)
            if not data: break
            if first and data[0:1] == b'\x16':
                for p in fragment(data):
                    dst.sendall(p); time.sleep(0.01)
                first = False
            else:
                dst.sendall(data); first = False
    except Exception:
        pass

def handle(client, addr):
    try:
        data = client.recv(BUFFER)
        if not data.startswith(b'CONNECT '): 
            client.sendall(b'HTTP/1.1 405\r\n\r\n'); return
        target = data.split(b'\r\n')[0].decode().split()[1]
        host, port = target.split(':') if ':' in target else (target, '443')
        remote = socket.create_connection((host, int(port)), timeout=12)
        client.sendall(b'HTTP/1.1 200 Connection Established\r\n\r\n')
        t1 = threading.Thread(target=forward, args=(client, remote, True), daemon=True)
        t2 = threading.Thread(target=forward, args=(remote, client, False), daemon=True)
        t1.start(); t2.start(); t1.join(); t2.join()
    except Exception as e:
        print('error', e)
    finally:
        client.close()

def main():
    s = socket.socket(); s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((LISTEN_HOST, LISTEN_PORT)); s.listen(50)
    print(f'proxy on {LISTEN_HOST}:{LISTEN_PORT}')
    while True:
        c, a = s.accept()
        threading.Thread(target=handle, args=(c, a), daemon=True).start()

if __name__ == '__main__':
    main()
