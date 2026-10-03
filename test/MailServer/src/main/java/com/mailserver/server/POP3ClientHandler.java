package com.mailserver.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.mailserver.dao.EmailDAO;
import com.mailserver.dao.UserDAO;
import com.mailserver.model.Email;
import com.mailserver.model.User;

public class POP3ClientHandler implements Runnable {

    private Socket socket;

    public POP3ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            PrintWriter writer = new PrintWriter(
                socket.getOutputStream(), true
            )
        ) {

            UserDAO userDAO = new UserDAO();
            EmailDAO emailDAO = new EmailDAO();

            String username = null;
            boolean authenticated = false;

            writer.println("+OK POP3 SERVER READY");

            while (true) {

                String command = reader.readLine();

                if (command == null) {
                    break;
                }

                System.out.println("POP3 << " + command);

                String upperCommand = command.toUpperCase();

                // ==========================================
                // USER
                // ==========================================

                if (upperCommand.startsWith("USER ")) {

                    username = command.substring(5).trim();

                    writer.println("+OK User accepted");
                }

                // ==========================================
                // PASS
                // ==========================================

                else if (upperCommand.startsWith("PASS ")) {

                    String password = command.substring(5).trim();

                    /*
                     * userDAO.login() trả về User, không phải boolean.
                     * Vì vậy phải kiểm tra user != null.
                     */

                    User user = null;

                    if (username != null) {
                        user = userDAO.login(username, password);
                    }

                    if (user != null) {

                        authenticated = true;

                        writer.println("+OK Login successful");

                        System.out.println(
                            "POP3 login success: " + username
                        );

                    } else {

                        authenticated = false;

                        writer.println("-ERR Login failed");
                    }
                }

                // ==========================================
                // STAT
                // ==========================================

                else if (upperCommand.equals("STAT")) {

                    if (!authenticated) {

                        writer.println("-ERR Not authenticated");
                        continue;
                    }

                    String emailAddress =
                        userDAO.getEmailByUsername(username);

                    List<Email> emails =
                        emailDAO.getInbox(emailAddress);

                    writer.println(
                        "+OK " + emails.size() + " messages"
                    );
                }

                // ==========================================
                // LIST
                // ==========================================

                else if (upperCommand.equals("LIST")) {

                    if (!authenticated) {

                        writer.println("-ERR Not authenticated");
                        continue;
                    }

                    String emailAddress =
                        userDAO.getEmailByUsername(username);

                    List<Email> emails =
                        emailDAO.getInbox(emailAddress);

                    writer.println(
                        "+OK " + emails.size() + " messages"
                    );

                    int number = 1;

                    for (Email email : emails) {

                        String subject =
                            email.getSubject() == null
                                ? ""
                                : email.getSubject();

                        String content =
                            email.getContent() == null
                                ? ""
                                : email.getContent();

                        int size =
                            subject.length()
                            + content.length();

                        writer.println(
                            number + " " + size
                        );

                        number++;
                    }

                    writer.println(".");
                }

                // ==========================================
                // RETR
                // ==========================================

                else if (upperCommand.startsWith("RETR ")) {

                    if (!authenticated) {

                        writer.println("-ERR Not authenticated");
                        continue;
                    }

                    try {

                        int messageNumber =
                            Integer.parseInt(
                                command.substring(5).trim()
                            );

                        String emailAddress =
                            userDAO.getEmailByUsername(username);

                        List<Email> emails =
                            emailDAO.getInbox(emailAddress);

                        if (messageNumber < 1
                                || messageNumber > emails.size()) {

                            writer.println(
                                "-ERR No such message"
                            );

                            continue;
                        }

                        Email email =
                            emails.get(messageNumber - 1);

                        writer.println(
                            "+OK Message follows"
                        );

                        writer.println(
                            "From: " + email.getSender()
                        );

                        writer.println(
                            "To: " + email.getReceiver()
                        );

                        writer.println(
                            "Subject: " + email.getSubject()
                        );

                        writer.println();

                        writer.println(
                            email.getContent() == null
                                ? ""
                                : email.getContent()
                        );

                        writer.println(".");

                        emailDAO.markAsRead(
                            email.getId(),
                            emailAddress
                        );

                    } catch (NumberFormatException e) {

                        writer.println(
                            "-ERR Invalid message number"
                        );
                    }
                }

                // ==========================================
                // QUIT
                // ==========================================

                else if (upperCommand.equals("QUIT")) {

                    writer.println("+OK Bye");

                    break;
                }

                // ==========================================
                // UNKNOWN COMMAND
                // ==========================================

                else {

                    writer.println(
                        "-ERR Command not implemented"
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                "POP3 client disconnected."
            );

        } finally {

            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }
}