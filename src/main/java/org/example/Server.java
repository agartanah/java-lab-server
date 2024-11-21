package org.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static String host;
    private static int port;

    public static void start() {
        loadConfig();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Loggers.getLoggerUser().info("Server started at {}:{}", host, port);
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            Loggers.getLoggerErrors().error("Server error: ", e);
        }
    }

    private static void loadConfig() {
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("server.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                throw new FileNotFoundException("Server properties file not found.");
            }
            prop.load(input);
            host = prop.getProperty("host");
            port = Integer.parseInt(prop.getProperty("port"));
        } catch (IOException e) {
            Loggers.getLoggerErrors().error("Failed to load server properties: ", e);
        }
    }

    static synchronized void broadcast(String message, String sender) {
        Loggers.getLoggerUser().info("Broadcast message from {}: {}", sender, message);
        clients.forEach((nickname, client) -> client.sendMessage("Broadcast from " + sender + ": " + message));
    }

    static synchronized void privateMessage(String recipient, String message, String sender) {
        ClientHandler client = clients.get(recipient);
        if (client != null) {
            Loggers.getLoggerUser().info("Private message from {} to {}: {}", sender, recipient, message);
            client.sendMessage("Private from " + sender + ": " + message);
        } else {
            Loggers.getLoggerErrors().warn("Attempted to send message to nonexistent user: {}", recipient);
            clients.get(sender).sendMessage("Attempted to send message to nonexistent user: " + recipient);
        }
    }

    static synchronized void addClient(String nickname, ClientHandler handler) {
        if (clients.containsKey(nickname)) {
            Loggers.getLoggerErrors().warn("This nickname exists: {}", nickname);
            throw new RuntimeException("This nickname exists");
        }

        clients.put(nickname, handler);
    }

    static synchronized void removeClient(String nickname) {
        clients.remove(nickname);
    }

    static synchronized List<String> getActiveUsers() {
        return new ArrayList<>(clients.keySet());
    }
}
