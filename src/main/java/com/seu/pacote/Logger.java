package com.seu.pacote;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Logger {
    private static Logger instance;
    private BufferedWriter writer;
    private boolean consoleOutput = true;
    private boolean fileOutput = true;
    private Process nodeProcess;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    public enum Level {
        SILLY, DEBUG, INFO, WARN, ERROR, FATAL
    }

    private Logger() {
        try {
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdir();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File logFile = new File("logs/debug_" + timestamp + ".log");
            writer = new BufferedWriter(new FileWriter(logFile));

            startNodeDebugViewer();
        } catch (IOException e) {
            System.err.println("Erro ao inicializar DebugLogger: " + e.getMessage());
        }
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    private void startNodeDebugViewer() {
        try {
            File nodeScript = new File("logs/debug_viewer.js");
            
            // Cria um script Node.js mais robusto
            try (FileWriter scriptWriter = new FileWriter(nodeScript)) {
                scriptWriter.write(
                "const fs = require('fs');\n" +
                "const http = require('http');\n" +
                "const path = require('path');\n" +
                "const WebSocket = require('ws');\n" +
                "const tail = require('tail');\n\n" +
                "const server = http.createServer((req, res) => {\n" +
                "  if (req.url === '/') {\n" +
                "    res.writeHead(200, {'Content-Type': 'text/html'});\n" +
                "    res.end(`\n" +
                "      <!DOCTYPE html>\n" +
                "      <html>\n" +
                "        <head>\n" +
                "          <title>Debug Viewer</title>\n" +
                "          <style>\n" +
                "            body { font-family: monospace; margin: 0; padding: 20px; background: #222; color: #eee; }\n" +
                "            #logs { white-space: pre; }\n" +
                "            .log-entry { margin-bottom: 5px; }\n" +
                "            .timestamp { color: #4CAF50; }\n" +
                "            .message { color: #fff; }\n" +
                "            .level-SILLY { color: #9E9E9E; }\n" +
                "            .level-DEBUG { color: #2196F3; }\n" +
                "            .level-INFO { color: #4CAF50; }\n" +
                "            .level-WARN { color: #FFC107; }\n" +
                "            .level-ERROR { color: #FF5722; }\n" +
                "            .level-FATAL { color: #F44336; font-weight: bold; }\n" +
                "          </style>\n" +
                "        </head>\n" +
                "        <body>\n" +
                "          <h1>Debug Viewer</h1>\n" +
                "          <div id=\"logs\"></div>\n" +
                "          <script>\n" +
                "            const ws = new WebSocket('ws://localhost:3001');\n" +
                "            ws.onmessage = (event) => {\n" +
                "              const log = JSON.parse(event.data);\n" +
                "              const logElement = document.createElement('div');\n" +
                "              logElement.className = 'log-entry';\n" +
                "              logElement.innerHTML = `\n" +
                "                <span class=\"timestamp\">${log.timestamp}</span>\n" +
                "                <span class=\"level level-${log.level}\">${log.level.padEnd(7)}</span>\n" +
                "                <span class=\"message\">${log.message}</span>\n" +
                "              `;\n" +
                "              document.getElementById('logs').appendChild(logElement);\n" +
                "              window.scrollTo(0, document.body.scrollHeight);\n" +
                "            };\n" +
                "          </script>\n" +
                "        </body>\n" +
                "      </html>\n" +
                "    `);\n" +
                "  }\n" +
                "});\n\n" +
                "const wss = new WebSocket.Server({ server });\n\n" +
                "// Monitora todos os arquivos de log no diretório\n" +
                "fs.watch('logs', (eventType, filename) => {\n" +
                "  if (filename && filename.endsWith('.log')) {\n" +
                "    const logFile = path.join('logs', filename);\n" +
                "    new tail.Tail(logFile).on('line', (line) => {\n" +
                "      try {\n" +
                "        // Parse a linha de log no formato específico\n" +
                "        const logMatch = line.match(/^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) (\\w+)\\s+(.*)$/);\n" +
                "        if (logMatch) {\n" +
                "          const timestamp = logMatch[1];\n" +
                "          const level = logMatch[2];\n" +
                "          const message = logMatch[3];\n" +
                "          \n" +
                "          // Envia para todos os clientes WebSocket conectados\n" +
                "          wss.clients.forEach(client => {\n" +
                "            if (client.readyState === WebSocket.OPEN) {\n" +
                "              client.send(JSON.stringify({ timestamp, level, message }));\n" +
                "            }\n" +
                "          });\n" +
                "        }\n" +
                "      } catch (e) { console.error('Error processing log line:', e); }\n" +
                "    });\n" +
                "  }\n" +
                "});\n\n" +
                "server.listen(3000, () => {\n" +
                "  console.log('Debug Viewer running at http://localhost:3002');\n" +
                "  console.log('Press Ctrl+C to stop');\n" +
                "});"
                );
            }

            // Instala a dependência 'tail' se não existir
            if (!new File("logs/node_modules/tail").exists()) {
                Runtime.getRuntime().exec("npm install tail --prefix logs", null, new File("logs"));
            }

            // Executa o script Node.js
            String[] command = {
                "node",
                nodeScript.getAbsolutePath()
            };
            nodeProcess = Runtime.getRuntime().exec(command);
            
            // Aguarda um pouco para o servidor iniciar
            Thread.sleep(1000);
            
            // Abre o navegador padrão
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "http://localhost:3002"});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", "http://localhost:3002"});
            } else {
                Runtime.getRuntime().exec(new String[]{"xdg-open", "http://localhost:3002"});
            }
        } catch (Exception e) {
            System.err.println("Erro ao iniciar Node.js debug viewer: " + e.getMessage());
        }
    }

    public void log(Level level, String message, Object... args) {
        String timestamp = LocalDateTime.now().format(dtf);
        String formattedMessage = formatMessage(message, args);
        String logMessage = String.format("%s %-7s %s", timestamp, level, formattedMessage);

        if (consoleOutput) {
            System.out.println(logMessage);
        }

        if (fileOutput && writer != null) {
            try {
                writer.write(logMessage);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                System.err.println("Erro ao escrever no arquivo de log: " + e.getMessage());
            }
        }
    }

    public void log(String message) {
        String timestamp = LocalDateTime.now().format(dtf);
        String logMessage = "[" + timestamp + "] " + message;

        if (consoleOutput) {
            System.out.println(logMessage);
        }

        if (fileOutput && writer != null) {
            try {
                writer.write(logMessage);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                System.err.println("Erro ao escrever no arquivo de log: " + e.getMessage());
            }
        }
    }

    private String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }

        try {
            // Substitui placeholders {} pelos argumentos
            String formatted = message;
            for (Object arg : args) {
                formatted = formatted.replaceFirst("\\{\\}", formatObject(arg));
            }
            return formatted;
        } catch (Exception e) {
            return message + " " + Arrays.toString(args);
        }
    }

    private String formatObject(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (obj instanceof Throwable) {
            return formatException((Throwable) obj);
        }

        // Formatação simples para objetos
        return obj.toString();
    }

    private String formatException(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return e.getMessage() + "\n" + sw.toString();
    }

    // Métodos auxiliares para cada nível de log
    public void silly(String message, Object... args) {
        log(Level.SILLY, message, args);
    }

    public void debug(String message, Object... args) {
        log(Level.DEBUG, message, args);
    }

    public void info(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public void warn(String message, Object... args) {
        log(Level.WARN, message, args);
    }

    public void error(String message, Object... args) {
        log(Level.ERROR, message, args);
    }

    public void fatal(String message, Object... args) {
        log(Level.FATAL, message, args);
    }

    public void enableConsoleOutput(boolean enable) {
        this.consoleOutput = enable;
    }

    public void enableFileOutput(boolean enable) {
        this.fileOutput = enable;
    }

    public void close() {
        try {
            if (writer != null) {
                String timestamp = LocalDateTime.now().format(dtf);
                writer.write(timestamp + " INFO    Logger sendo encerrado");
                writer.newLine();
                writer.close();
            }
            
            if (nodeProcess != null && nodeProcess.isAlive()) {
                nodeProcess.destroy();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar logger: " + e.getMessage());
        }
    }
}
