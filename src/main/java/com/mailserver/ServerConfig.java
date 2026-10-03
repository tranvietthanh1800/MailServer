package com.mailserver;

public class ServerConfig {

    private static String serverHost = " 192.168.1.5";

    public static final int MAIL_PORT = 2525;
    public static final int SMTP_PORT = 2526;
    public static final int POP3_PORT = 1110;
    public static final int IMAP_PORT = 1430;

    public static String getServerHost() {
        return serverHost;
    }

    public static void setServerHost(String host) {
        if (host != null && !host.trim().isEmpty()) {
            serverHost = host.trim();
        }
    }

    private ServerConfig() {
    }
}