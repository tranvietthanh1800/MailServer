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

public class IMAPClientHandler implements Runnable {

    private Socket socket;

    public IMAPClientHandler(Socket socket) {
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

            writer.println("* OK IMAP SERVER READY");

            while (true) {

                String command = reader.readLine();

                if (command == null) {
                    break;
                }

                System.out.println("IMAP << " + command);

                String upperCommand = command.toUpperCase();

                // ==========================================
                // LOGIN
                // ==========================================

                if (upperCommand.startsWith("A001 LOGIN ")) {

                    String data = command.substring(11).trim();

                    String[] parts = data.split("\\s+");

                    if (parts.length >= 2) {

                        username = parts[0];
                        String password = parts[1];

                        /*
                         * userDAO.login() trả về User,
                         * không phải boolean.
                         */
                        User user = userDAO.login(
                            username,
                            password
                        );

                        if (user != null) {

                            authenticated = true;

                            writer.println(
                                "A001 OK LOGIN completed"
                            );

                            System.out.println(
                                "IMAP login success: " + username
                            );

                        } else {

                            authenticated = false;

                            writer.println(
                                "A001 NO LOGIN failed"
                            );
                        }

                    } else {

                        writer.println(
                            "A001 BAD Invalid LOGIN command"
                        );
                    }
                }

                // ==========================================
                // LIST MAILBOX
                // ==========================================

                else if (upperCommand.startsWith("A002 LIST")) {

                    if (!authenticated) {

                        writer.println(
                            "A002 NO Not authenticated"
                        );

                        continue;
                    }

                    writer.println(
                        "* LIST () \"/\" INBOX"
                    );

                    writer.println(
                        "A002 OK LIST completed"
                    );
                }

                // ==========================================
                // SELECT INBOX
                // ==========================================

                else if (
                    upperCommand.startsWith("A003 SELECT INBOX")
                ) {

                    if (!authenticated) {

                        writer.println(
                            "A003 NO Not authenticated"
                        );

                        continue;
                    }

                    String emailAddress =
                        userDAO.getEmailByUsername(username);

                    List<Email> emails =
                        emailDAO.getInbox(emailAddress);

                    int exists = emails.size();

                    writer.println(
                        "* " + exists + " EXISTS"
                    );

                    writer.println(
                        "* FLAGS (\\Seen \\Deleted)"
                    );

                    writer.println(
                        "A003 OK [READ-WRITE] SELECT completed"
                    );
                }

                // ==========================================
                // FETCH
                // ==========================================

                else if (
                    upperCommand.startsWith("A004 FETCH")
                ) {

                    if (!authenticated) {

                        writer.println(
                            "A004 NO Not authenticated"
                        );

                        continue;
                    }

                    String emailAddress =
                        userDAO.getEmailByUsername(username);

                    List<Email> emails =
                        emailDAO.getInbox(emailAddress);

                    for (int i = 0; i < emails.size(); i++) {

                        Email email = emails.get(i);

                        String sender =
                            email.getSender() == null
                                ? ""
                                : email.getSender();

                        String receiver =
                            email.getReceiver() == null
                                ? ""
                                : email.getReceiver();

                        String subject =
                            email.getSubject() == null
                                ? ""
                                : email.getSubject();

                        writer.println(
                            "* " + (i + 1)
                            + " FETCH ("
                            + "FROM \"" + sender + "\" "
                            + "TO \"" + receiver + "\" "
                            + "SUBJECT \"" + subject + "\""
                            + ")"
                        );
                    }

                    writer.println(
                        "A004 OK FETCH completed"
                    );
                }

                // ==========================================
                // LOGOUT
                // ==========================================

                else if (
                    upperCommand.startsWith("A005 LOGOUT")
                ) {

                    writer.println(
                        "* BYE IMAP Server logging out"
                    );

                    writer.println(
                        "A005 OK LOGOUT completed"
                    );

                    break;
                }

                // ==========================================
                // UNKNOWN COMMAND
                // ==========================================

                else {

                    writer.println(
                        "BAD Command not implemented"
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                "IMAP client disconnected."
            );

        } finally {

            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }
}