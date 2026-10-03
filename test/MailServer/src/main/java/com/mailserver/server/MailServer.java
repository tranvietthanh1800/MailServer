package com.mailserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MailServer {

        private static final int PORT = 2525;

        public static void main(String[] args) {

                ServerLogger.log("=================================");
                ServerLogger.log("       MAIL SERVER STARTING");
                ServerLogger.log("=================================");

                try (ServerSocket serverSocket = new ServerSocket(PORT)) {

                        ServerLogger.log(
                                        "Server dang chay tai port: " + PORT);

                        ServerLogger.log(
                                        "Dang cho Client ket noi...");

                        while (true) {

                                Socket clientSocket = serverSocket.accept();

                                ServerLogger.log(
                                                "Client ket noi: "
                                                                + clientSocket.getInetAddress()
                                                                + ":"
                                                                + clientSocket.getPort());

                                ClientHandler clientHandler = new ClientHandler(clientSocket);

                                Thread thread = new Thread(clientHandler);

                                thread.start();

                                ServerLogger.log(
                                                "ClientHandler da duoc tao cho client "
                                                                + clientSocket.getInetAddress());
                        }

                } catch (IOException e) {

                        ServerLogger.log(
                                        "Khong the khoi dong Server!");

                        ServerLogger.log(
                                        "ERROR: " + e.getMessage());

                        e.printStackTrace();
                }
        }
}