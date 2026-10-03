package com.mailserver;

import com.mailserver.server.IMAPServer;
import com.mailserver.server.MailServer;
import com.mailserver.server.POP3Server;
import com.mailserver.server.SMTPServer;

public class Main {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("          MAIL SERVER SYSTEM");
        System.out.println("========================================");

        // ========================================
        // MAIL SERVER
        // ========================================

        Thread mailThread = new Thread(() -> {
            MailServer.main(new String[] {});
        });
        // ========================================
        // SMTP SERVER
        // ========================================

        Thread smtpThread = new Thread(() -> {
            new SMTPServer().run();
        });

        // ========================================
        // POP3 SERVER
        // ========================================

        Thread pop3Thread = new Thread(() -> {
            new POP3Server().run();
        });

        // ========================================
        // IMAP SERVER
        // ========================================

        Thread imapThread = new Thread(() -> {
            new IMAPServer().run();
        });

        // ========================================
        // START
        // ========================================

        System.out.println("[START] Mail Server...");
        mailThread.start();

        System.out.println("[START] SMTP Server...");
        smtpThread.start();

        System.out.println("[START] POP3 Server...");
        pop3Thread.start();

        System.out.println("[START] IMAP Server...");
        imapThread.start();

        System.out.println();
        System.out.println("========================================");
        System.out.println("       ALL SERVICES STARTED");
        System.out.println("========================================");
        System.out.println("Mail  Server : 2525");
        System.out.println("SMTP  Server : 2526");
        System.out.println("POP3  Server : 1110");
        System.out.println("IMAP  Server : 1430");
        System.out.println("========================================");
    }
}