package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket socket;
    private String nickname;
    private PrintWriter out;
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);


    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            this.out = out;

            while (true) {
                out.println("Enter your nickname:");

                nickname = in.readLine();

                try {
                    Server.addClient(nickname, this);
                } catch (RuntimeException e) {
                    continue;
                }

                break;
            }


            String input;
            while ((input = in.readLine()) != null) {
                String[] parts = input.split(" ", 2);
                String command = parts[0];
                String content = parts.length > 1 ? parts[1] : "";
                handleCommand(command, content);
            }
        } catch (IOException e) {
            logger.warn("Error handling client {}: ", nickname, e);
        } finally {
            Server.removeClient(nickname);
        }
    }

    private void handleCommand(String command, String content) {
        switch (command) {
            case "/broadcast":
                Server.broadcast(content, nickname);

                break;
            case "/private":
                String[] parts = content.split(" ", 2);

                if (parts.length == 2) {
                    Server.privateMessage(parts[0], parts[1], nickname);
                }

                break;
            case "/list":
                sendMessage("Active users: " + Server.getActiveUsers());

                break;
            default:
                sendMessage("Unknown command.");
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}