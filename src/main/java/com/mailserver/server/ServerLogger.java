package com.mailserver.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = LOG_DIR + File.separator + "server.log";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Object LOCK = new Object();

    static {
        createLogDirectory();
    }

    private static void createLogDirectory() {

        File directory = new File(LOG_DIR);

        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static void log(String message) {

        String time = LocalDateTime.now().format(FORMATTER);

        String logMessage = "[" + time + "] " + message;

        // Hiện trên console
        System.out.println(logMessage);

        // Ghi vào file
        synchronized (LOCK) {

            try (
                    FileWriter writer = new FileWriter(LOG_FILE, true);

                    PrintWriter out = new PrintWriter(writer)) {

                out.println(logMessage);

            } catch (IOException e) {

                System.err.println(
                        "Cannot write server log: "
                                + e.getMessage());
            }
        }
    }

    public static String getLogFilePath() {

        return LOG_FILE;
    }

    public static void clearLog() {

        synchronized (LOCK) {

            try (
                    FileWriter writer = new FileWriter(LOG_FILE, false);

                    PrintWriter out = new PrintWriter(writer)) {

                out.print("");

            } catch (IOException e) {

                System.err.println(
                        "Cannot clear server log: "
                                + e.getMessage());
            }
        }
    }
}