const fs = require('fs');
const http = require('http');
const path = require('path');
const WebSocket = require('ws');
const tail = require('tail');

const server = http.createServer((req, res) => {
  if (req.url === '/') {
    res.writeHead(200, {'Content-Type': 'text/html'});
    res.end(`
      <!DOCTYPE html>
      <html>
        <head>
          <title>Debug Viewer</title>
          <style>
            body { font-family: monospace; margin: 0; padding: 20px; background: #222; color: #eee; }
            #logs { white-space: pre; }
            .log-entry { margin-bottom: 5px; }
            .timestamp { color: #4CAF50; }
            .message { color: #fff; }
            .level-SILLY { color: #9E9E9E; }
            .level-DEBUG { color: #2196F3; }
            .level-INFO { color: #4CAF50; }
            .level-WARN { color: #FFC107; }
            .level-ERROR { color: #FF5722; }
            .level-FATAL { color: #F44336; font-weight: bold; }
          </style>
        </head>
        <body>
          <h1>Debug Viewer</h1>
          <div id="logs"></div>
          <script>
            const ws = new WebSocket('ws://localhost:3001');
            ws.onmessage = (event) => {
              const log = JSON.parse(event.data);
              const logElement = document.createElement('div');
              logElement.className = 'log-entry';
              logElement.innerHTML = `
                <span class="timestamp">${log.timestamp}</span>
                <span class="level level-${log.level}">${log.level.padEnd(7)}</span>
                <span class="message">${log.message}</span>
              `;
              document.getElementById('logs').appendChild(logElement);
              window.scrollTo(0, document.body.scrollHeight);
            };
          </script>
        </body>
      </html>
    `);
  }
});

const wss = new WebSocket.Server({ server });

// Monitora todos os arquivos de log no diretório
fs.watch('logs', (eventType, filename) => {
  if (filename && filename.endsWith('.log')) {
    const logFile = path.join('logs', filename);
    new tail.Tail(logFile).on('line', (line) => {
      try {
        // Parse a linha de log no formato específico
        const logMatch = line.match(/^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}) (\w+)\s+(.*)$/);
        if (logMatch) {
          const timestamp = logMatch[1];
          const level = logMatch[2];
          const message = logMatch[3];
          
          // Envia para todos os clientes WebSocket conectados
          wss.clients.forEach(client => {
            if (client.readyState === WebSocket.OPEN) {
              client.send(JSON.stringify({ timestamp, level, message }));
            }
          });
        }
      } catch (e) { console.error('Error processing log line:', e); }
    });
  }
});

server.listen(3000, () => {
  console.log('Debug Viewer running at http://localhost:3002');
  console.log('Press Ctrl+C to stop');
});