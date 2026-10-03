package com.mailserver;

import com.mailserver.server.IMAPServer;
import com.mailserver.server.MailServer;
import com.mailserver.server.POP3Server;
import com.mailserver.server.SMTPServer;

public class Main {

        public static void main(String[] args) {

                System.out.println();
                System.out.println("========================================");
                System.out.println("          MAIL SERVER SYSTEM");
                System.out.println("========================================");
                System.out.println();

                System.out.println(
                                "Server IP: "
                                                + ServerConfig.getServerHost());

                System.out.println(
                                "Mail Port: "
                                                + ServerConfig.MAIL_PORT);

                System.out.println(
                                "SMTP Port: "
                                                + ServerConfig.SMTP_PORT);

                System.out.println(
                                "POP3 Port: "
                                                + ServerConfig.POP3_PORT);

                System.out.println(
                                "IMAP Port: "
                                                + ServerConfig.IMAP_PORT);

                System.out.println();
                System.out.println("Starting services...");
                System.out.println();

                // ==========================================
                // MAIL SERVER
                // ==========================================

                Thread mailThread = new Thread(
                                new MailServer(),
                                "MailServer-Thread");

                // ==========================================
                // SMTP SERVER
                // ==========================================

                Thread smtpThread = new Thread(
                                new SMTPServer(),
                                "SMTPServer-Thread");

                // ==========================================
                // POP3 SERVER
                // ==========================================

                Thread pop3Thread = new Thread(
                                new POP3Server(),
                                "POP3Server-Thread");

                // ==========================================
                // IMAP SERVER
                // ==========================================

                Thread imapThread = new Thread(
                                new IMAPServer(),
                                "IMAPServer-Thread");

                // ==========================================
                // START
                // ==========================================

                mailThread.start();
                smtpThread.start();
                pop3Thread.start();
                imapThread.start();

                System.out.println(
                                "All services have been started.");
        }
}