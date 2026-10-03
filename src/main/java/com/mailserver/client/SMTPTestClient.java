package com.mailserver.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.mailserver.ServerConfig;

public class SMTPTestClient {

    private static final String HOST = ServerConfig.getServerHost();

    private static final int PORT = ServerConfig.SMTP_PORT;

    public static void main(String[] args) {

        try (
                Socket socket = new Socket(HOST, PORT);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                PrintWriter writer = new PrintWriter(
                        socket.getOutputStream(), true);

                BufferedReader keyboard = new BufferedReader(
                        new InputStreamReader(System.in))) {

            System.out.println("========================================");
            System.out.println("          SMTP TEST CLIENT");
            System.out.println("========================================");
            System.out.println("Connected to SMTP Server: "
                    + HOST + ":" + PORT);
            System.out.println();

            // Nhận greeting từ SMTP Server
            printServerResponse(reader);

            // HELO
            System.out.println(">> HELO localhost");
            writer.println("HELO localhost");
            printServerResponse(reader);

            // Nhập email người gửi
            System.out.print("Sender email: ");
            String sender = keyboard.readLine();

            System.out.println(">> MAIL FROM: " + sender);
            writer.println("MAIL FROM: " + sender);
            printServerResponse(reader);

            // Nhập email người nhận
            System.out.print("Receiver email: ");
            String receiver = keyboard.readLine();

            System.out.println(">> RCPT TO: " + receiver);
            writer.println("RCPT TO: " + receiver);
            printServerResponse(reader);

            // DATA
            System.out.println(">> DATA");
            writer.println("DATA");
            printServerResponse(reader);

            // Nhập Subject
            System.out.print("Subject: ");
            String subject = keyboard.readLine();

            // Nhập nội dung
            System.out.println("Content");
            System.out.println("(Nhap noi dung, go END de ket thuc)");

            StringBuilder content = new StringBuilder();

            while (true) {
                String line = keyboard.readLine();

                if ("END".equalsIgnoreCase(line)) {
                    break;
                }

                content.append(line).append("\n");
            }

            // Gửi Subject
            writer.println("Subject: " + subject);

            // Gửi Content
            writer.print(content.toString());

            // Kết thúc DATA
            writer.println("<END>");

            // Nhận kết quả
            printServerResponse(reader);

            // QUIT
            System.out.println(">> QUIT");
            writer.println("QUIT");
            printServerResponse(reader);

            System.out.println();
            System.out.println("========================================");
            System.out.println("          SMTP TEST COMPLETED");
            System.out.println("========================================");

        } catch (Exception e) {

            System.out.println();
            System.out.println("SMTP Client error:");
            e.printStackTrace();
        }
    }

    private static void printServerResponse(BufferedReader reader)
            throws Exception {

        String response = reader.readLine();

        if (response != null) {
            System.out.println("<< " + response);
        }
    }
}