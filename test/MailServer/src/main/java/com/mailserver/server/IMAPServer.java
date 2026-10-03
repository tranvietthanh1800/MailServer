package com.mailserver.server;

import java.net.ServerSocket;
import java.net.Socket;

public class IMAPServer implements Runnable {

    private static final int IMAP_PORT = 1430;

    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(IMAP_PORT)) {

            System.out.println("========================================");
            System.out.println("           IMAP SERVER");
            System.out.println("========================================");
            System.out.println("IMAP Server started on port " + IMAP_PORT);

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                    "IMAP client connected: "
                    + socket.getInetAddress()
                );

                Thread clientThread =
                    new Thread(new IMAPClientHandler(socket));

                clientThread.start();
            }

        } catch (Exception e) {

            System.out.println("IMAP Server error.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new IMAPServer().run();
    }
}