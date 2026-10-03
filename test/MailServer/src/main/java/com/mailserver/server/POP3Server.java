package com.mailserver.server;

import java.net.ServerSocket;
import java.net.Socket;

public class POP3Server implements Runnable {

    private static final int POP3_PORT = 1110;

    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(POP3_PORT)) {

            System.out.println("========================================");
            System.out.println("           POP3 SERVER");
            System.out.println("========================================");
            System.out.println("POP3 Server started on port " + POP3_PORT);

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                    "POP3 client connected: "
                    + socket.getInetAddress()
                );

                Thread clientThread =
                    new Thread(new POP3ClientHandler(socket));

                clientThread.start();
            }

        } catch (Exception e) {

            System.out.println("POP3 Server error.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new POP3Server().run();
    }
}