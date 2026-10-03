package com.mailserver.server;

import java.net.ServerSocket;
import java.net.Socket;

import com.mailserver.ServerConfig;

public class IMAPServer implements Runnable {

    private final int port;

    public IMAPServer() {
        this.port = ServerConfig.IMAP_PORT;
    }

    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("========================================");
            System.out.println("             IMAP SERVER");
            System.out.println("========================================");
            System.out.println("Port: " + port);
            System.out.println("Waiting for IMAP clients...");

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                        "[IMAP] Client connected: "
                                + socket.getInetAddress());

                Thread thread =
                        new Thread(
                                new IMAPClientHandler(socket));

                thread.start();
            }

        } catch (Exception e) {

            System.out.println("[IMAP] Server error:");
            e.printStackTrace();
        }
    }
}