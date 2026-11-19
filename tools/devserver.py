"""本機預覽用的靜態網頁伺服器：每個回應都加上 Cache-Control: no-store，
瀏覽器就不會拿舊的 CSS/JS 來用，改完檔案按 F5 就是最新版。

用法：python -I tools/devserver.py <frontend 資料夾> <port>
（dev.ps1 會自動用這個啟動前端）
"""
import functools
import http.server
import os
import sys
import time


class NoCacheHandler(http.server.SimpleHTTPRequestHandler):
    # Windows 的登錄檔有時會把 .js 對應成 text/plain，這裡明確指定
    extensions_map = {
        **http.server.SimpleHTTPRequestHandler.extensions_map,
        ".html": "text/html; charset=utf-8",
        ".js": "text/javascript; charset=utf-8",
        ".css": "text/css; charset=utf-8",
        ".json": "application/json",
        ".jpg": "image/jpeg",
        ".png": "image/png",
        ".svg": "image/svg+xml",
    }

    def end_headers(self):
        self.send_header("Cache-Control", "no-store")
        super().end_headers()

    def do_GET(self):
        """HTML 裡的 ?v=dev 每次都換成當下時間，瀏覽器就算以前快取過舊檔也一定重抓。"""
        path = self.translate_path(self.path)
        if os.path.isdir(path):
            path = os.path.join(path, "index.html")
        if not path.endswith(".html") or not os.path.isfile(path):
            return super().do_GET()
        with open(path, "rb") as f:
            body = f.read().replace(b"?v=dev", f"?v={time.time_ns()}".encode())
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


def main():
    root, port = sys.argv[1], int(sys.argv[2])
    handler = functools.partial(NoCacheHandler, directory=root)
    server = http.server.ThreadingHTTPServer(("127.0.0.1", port), handler)
    print(f"前端預覽：http://localhost:{port}  （不快取，改檔案後按 F5 即可）")
    server.serve_forever()


if __name__ == "__main__":
    main()
