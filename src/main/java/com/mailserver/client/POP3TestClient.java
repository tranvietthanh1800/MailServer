package com.mailserver.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class POP3TestClient {

    private static final String HOST = "localhost";
    private static final int PORT = 1110;

    public static void main(String[] args) {

        try (
            Socket socket = new Socket(HOST, PORT);

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            PrintWriter writer = new PrintWriter(
                socket.getOutputStream(), true
            );

            BufferedReader keyboard = new BufferedReader(
                new InputStreamReader(System.in)
            )
        ) {

            System.out.println("========================================");
            System.out.println("          POP3 TEST CLIENT");
            System.out.println("========================================");

            System.out.println(
                "Connected to POP3 Server: "
                + HOST + ":" + PORT
            );

            System.out.println();

            // Greeting
            printResponse(reader);

            // USER
            System.out.print("Username: ");
            String username = keyboard.readLine();

            System.out.println(">> USER " + username);

            writer.println("USER " + username);

            printResponse(reader);

            // PASS
            System.out.print("Password: ");
            String password = keyboard.readLine();

            System.out.println(">> PASS ******");

            writer.println("PASS " + password);

            printResponse(reader);

            // STAT
            System.out.println();
            System.out.println(">> STAT");

            writer.println("STAT");

            printResponse(reader);

            // LIST
            System.out.println();
            System.out.println(">> LIST");

            writer.println("LIST");

            readMultiLineResponse(reader);

            // RETR
            System.out.println();
            System.out.print("Nhap so thu tu email muon doc: ");

            String messageNumber = keyboard.readLine();

            System.out.println(
                ">> RETR " + messageNumber
            );

            writer.println("RETR " + messageNumber);

            readMultiLineResponse(reader);

            // QUIT
            System.out.println();
            System.out.println(">> QUIT");

            writer.println("QUIT");

            printResponse(reader);

            System.out.println();
            System.out.println("========================================");
            System.out.println("          POP3 TEST COMPLETED");
            System.out.println("========================================");

        } catch (Exception e) {

            System.out.println();
            System.out.println("POP3 Client error:");
            e.printStackTrace();
        }
    }

    private static void printResponse(
            BufferedReader reader) throws Exception {

        String response = reader.readLine();

        if (response != null) {
            System.out.println("<< " + response);
        }
    }

    private static void readMultiLineResponse(
            BufferedReader reader) throws Exception {

        String response = reader.readLine();

        if (response == null) {
            return;
        }

        System.out.println("<< " + response);

        if (response.startsWith("+OK")) {

            while (true) {

                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                if (line.equals(".")) {
                    break;
                }

                System.out.println("   " + line);
            }
        }
    }
}