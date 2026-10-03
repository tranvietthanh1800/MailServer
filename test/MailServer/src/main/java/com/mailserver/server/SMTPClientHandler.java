package com.mailserver.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.mailserver.dao.EmailDAO;
import com.mailserver.model.Email;

public class SMTPClientHandler implements Runnable {

    private Socket socket;

    public SMTPClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()));

                PrintWriter writer =
                        new PrintWriter(
                                socket.getOutputStream(),
                                true)
        ) {

            EmailDAO emailDAO = new EmailDAO();

            // SMTP greeting
            writer.println("220 MAIL SERVER SMTP READY");

            String sender = null;
            String receiver = null;

            while (true) {

                String command = reader.readLine();

                if (command == null) {
                    break;
                }

                System.out.println(
                        "SMTP << " + command);


                // =========================
                // HELO
                // =========================
                if (command.toUpperCase()
                        .startsWith("HELO")) {

                    writer.println(
                            "250 Hello");
                }


                // =========================
                // MAIL FROM
                // =========================
                else if (command.toUpperCase()
                        .startsWith("MAIL FROM:")) {

                    sender =
                            command.substring(10)
                                    .trim();

                    writer.println(
                            "250 Sender OK");
                }


                // =========================
                // RCPT TO
                // =========================
                else if (command.toUpperCase()
                        .startsWith("RCPT TO:")) {

                    receiver =
                            command.substring(8)
                                    .trim();

                    writer.println(
                            "250 Receiver OK");
                }


                // =========================
                // DATA
                // =========================
                else if (command.equalsIgnoreCase("DATA")) {

                    if (sender == null ||
                            receiver == null) {

                        writer.println(
                                "503 Bad sequence");

                        continue;
                    }

                    writer.println(
                            "354 End data with <END>");

                    String subject = "";
                    String content = "";

                    while (true) {

                        String line =
                                reader.readLine();

                        if (line == null) {
                            break;
                        }

                        if (line.equals("<END>")) {
                            break;
                        }

                        if (line.startsWith(
                                "Subject:")) {

                            subject =
                                    line.substring(8)
                                            .trim();

                        } else {

                            content += line + "\n";
                        }
                    }

                    Email email =
                            new Email(
                                    sender,
                                    receiver,
                                    subject,
                                    content);

                    boolean success =
                            emailDAO.sendEmail(email);

                    if (success) {

                        writer.println(
                                "250 Message accepted");

                        System.out.println(
                                "SMTP mail saved: "
                                        + sender
                                        + " -> "
                                        + receiver);

                    } else {

                        writer.println(
                                "550 Message rejected");
                    }

                    sender = null;
                    receiver = null;
                }


                // =========================
                // QUIT
                // =========================
                else if (command.equalsIgnoreCase(
                        "QUIT")) {

                    writer.println(
                            "221 Bye");

                    break;
                }


                // =========================
                // UNKNOWN COMMAND
                // =========================
                else {

                    writer.println(
                            "502 Command not implemented");
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "SMTP client disconnected.");

        } finally {

            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }
}