package com.mailserver.server;

import java.net.ServerSocket;
import java.net.Socket;

import com.mailserver.ServerConfig;

public class POP3Server implements Runnable {

    private final int port;

    public POP3Server() {
        this.port = ServerConfig.POP3_PORT;
    }

    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("========================================");
            System.out.println("             POP3 SERVER");
            System.out.println("========================================");
            System.out.println("Port: " + port);
            System.out.println("Waiting for POP3 clients...");

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                        "[POP3] Client connected: "
                                + socket.getInetAddress());

                Thread thread = new Thread(
                        new POP3ClientHandler(socket));

                thread.start();
            }

        } catch (Exception e) {

            System.out.println("[POP3] Server error:");
            e.printStackTrace();
        }
    }
}