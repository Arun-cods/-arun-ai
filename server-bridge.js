const http = require('http');

const PORT = 3000;
const TARGET_HOST = '127.0.0.1';
const TARGET_PORT = 8080;

const server = http.createServer((req, res) => {
    const options = {
        hostname: TARGET_HOST,
        port: TARGET_PORT,
        path: req.url,
        method: req.method,
        headers: {
            ...req.headers,
            host: `${TARGET_HOST}:${TARGET_PORT}`
        }
    };

    const proxyReq = http.request(options, (proxyRes) => {
        res.writeHead(proxyRes.statusCode, proxyRes.headers);
        proxyRes.pipe(res);
    });

    proxyReq.on('error', (err) => {
        res.writeHead(502, { 'Content-Type': 'text/plain' });
        res.end('Arun AI Backend Starting or Unreachable: ' + err.message);
    });

    req.pipe(proxyReq);
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`Arun AI Mobile Bridge listening on http://0.0.0.0:${PORT} -> http://${TARGET_HOST}:${TARGET_PORT}`);
});
