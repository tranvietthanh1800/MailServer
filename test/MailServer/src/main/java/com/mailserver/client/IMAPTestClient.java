package com.mailserver.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class IMAPTestClient {

    private static final String HOST = "localhost";
    private static final int PORT = 1430;

    public static void main(String[] args) {

        try (
            Socket socket = new Socket(HOST, PORT);

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            PrintWriter writer = new PrintWriter(
                socket.getOutputStream(), true
            )
        ) {

            System.out.println("========================================");
            System.out.println("          IMAP TEST CLIENT");
            System.out.println("========================================");

            System.out.println(
                "Connected to IMAP Server: "
                + HOST + ":" + PORT
            );

            System.out.println();

            // Greeting
            readResponse(reader);

            // LOGIN
            System.out.println(
                ">> A001 LOGIN thanh 123456"
            );

            writer.println(
                "A001 LOGIN thanh 123456"
            );

            readResponse(reader);

            // LIST
            System.out.println();
            System.out.println(
                ">> A002 LIST \"\" \"*\""
            );

            writer.println(
                "A002 LIST \"\" \"*\""
            );

            readUntilTaggedResponse(reader, "A002");

            // SELECT INBOX
            System.out.println();
            System.out.println(
                ">> A003 SELECT INBOX"
            );

            writer.println(
                "A003 SELECT INBOX"
            );

            readUntilTaggedResponse(reader, "A003");

            // FETCH
            System.out.println();
            System.out.println(
                ">> A004 FETCH 1:*"
            );

            writer.println(
                "A004 FETCH 1:*"
            );

            readUntilTaggedResponse(reader, "A004");

            // LOGOUT
            System.out.println();
            System.out.println(
                ">> A005 LOGOUT"
            );

            writer.println(
                "A005 LOGOUT"
            );

            readUntilTaggedResponse(reader, "A005");

            System.out.println();
            System.out.println("========================================");
            System.out.println("          IMAP TEST COMPLETED");
            System.out.println("========================================");

        } catch (Exception e) {

            System.out.println();
            System.out.println("IMAP Client error:");
            e.printStackTrace();
        }
    }

    private static void readResponse(
            BufferedReader reader) throws Exception {

        String line = reader.readLine();

        if (line != null) {
            System.out.println("<< " + line);
        }
    }

    private static void readUntilTaggedResponse(
            BufferedReader reader,
            String tag) throws Exception {

        while (true) {

            String line = reader.readLine();

            if (line == null) {
                break;
            }

            System.out.println("<< " + line);

            if (line.startsWith(tag + " ")) {
                break;
            }
        }
    }
}