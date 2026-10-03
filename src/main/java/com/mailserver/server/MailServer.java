package com.mailserver.server;

import java.net.ServerSocket;
import java.net.Socket;

import com.mailserver.ServerConfig;

public class MailServer implements Runnable {

    private final int port;

    public MailServer() {
        this.port = ServerConfig.MAIL_PORT;
    }

    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("========================================");
            System.out.println("             MAIL SERVER");
            System.out.println("========================================");
            System.out.println("Port: " + port);
            System.out.println("Waiting for clients...");

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                        "[MAIL] Client connected: "
                                + socket.getInetAddress());

                Thread clientThread = new Thread(
                        new ClientHandler(socket));

                clientThread.start();
            }

        } catch (Exception e) {

            System.out.println("[MAIL] Server error:");
            e.printStackTrace();
        }
    }
}