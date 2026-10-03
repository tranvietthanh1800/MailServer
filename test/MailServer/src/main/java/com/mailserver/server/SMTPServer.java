package com.mailserver.server;

import java.net.ServerSocket;
import java.net.Socket;

public class SMTPServer implements Runnable {

    private static final int SMTP_PORT = 2526;

    @Override
    public void run() {

        try (ServerSocket serverSocket =
                     new ServerSocket(SMTP_PORT)) {

            System.out.println(
                    "SMTP Server started on port "
                            + SMTP_PORT);

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                        "SMTP client connected: "
                                + socket.getInetAddress());

                Thread clientThread =
                        new Thread(
                                new SMTPClientHandler(socket));

                clientThread.start();
            }

        } catch (Exception e) {

            System.out.println(
                    "SMTP Server error.");

            e.printStackTrace();
        }
    }


    // Dùng để test SMTP trực tiếp
    public static void main(String[] args) {

        new SMTPServer().run();
    }
}