package com.mailserver.server;

import java.net.ServerSocket;
import java.net.Socket;

import com.mailserver.ServerConfig;

public class SMTPServer implements Runnable {

    private final int port;

    public SMTPServer() {
        this.port = ServerConfig.SMTP_PORT;
    }

    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("========================================");
            System.out.println("             SMTP SERVER");
            System.out.println("========================================");
            System.out.println("Port: " + port);
            System.out.println("Waiting for SMTP clients...");

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                        "[SMTP] Client connected: "
                                + socket.getInetAddress());

                Thread thread =
                        new Thread(
                                new SMTPClientHandler(socket));

                thread.start();
            }

        } catch (Exception e) {

            System.out.println("[SMTP] Server error:");
            e.printStackTrace();
        }
    }
}