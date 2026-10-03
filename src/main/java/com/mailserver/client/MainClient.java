package com.mailserver.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MainClient {

        public static void main(String[] args) {

                try (
                                Socket socket = new Socket("localhost", 2525);

                                BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(socket.getInputStream()));

                                PrintWriter writer = new PrintWriter(
                                                socket.getOutputStream(), true);

                                Scanner scanner = new Scanner(System.in)) {

                        // =========================
                        // LOGIN
                        // =========================

                        System.out.println(reader.readLine());

                        // Username
                        System.out.print("Username: ");
                        reader.readLine(); // Username:
                        writer.println(scanner.nextLine());

                        // Password
                        System.out.print("Password: ");
                        reader.readLine(); // Password:
                        writer.println(scanner.nextLine());

                        String loginResult = reader.readLine();

                        System.out.println(loginResult);

                        if (!"LOGIN_SUCCESS".equals(loginResult)) {
                                System.out.println("Dang nhap that bai!");
                                return;
                        }

                        // =========================
                        // MAIN MENU
                        // =========================

                        while (true) {

                                System.out.println();
                                System.out.println("========== MAIL MENU ==========");
                                String menu1 = reader.readLine();
                                String menu2 = reader.readLine();
                                String menu3 = reader.readLine();
                                String menu4 = reader.readLine();
                                String menu5 = reader.readLine();
                                String menu6 = reader.readLine();
                                String menu7 = reader.readLine();
                                String menu8 = reader.readLine();
                                String menu9 = reader.readLine();
                                String menu10 = reader.readLine();
                                String choose = reader.readLine();

                                System.out.println(menu1);
                                System.out.println(menu2);
                                System.out.println(menu3);
                                System.out.println(menu4);
                                System.out.println(menu5);
                                System.out.println(menu6);
                                System.out.println(menu7);
                                System.out.println(menu8);
                                System.out.println(menu9);
                                System.out.println(menu10);
                                System.out.println(choose);

                                System.out.print("> ");

                                String choice = scanner.nextLine();

                                writer.println(choice);
                                // =========================
                                // 1. SEND MAIL
                                // =========================

                                if ("1".equals(choice)) {

                                        // Receiver
                                        String receiverPrompt = reader.readLine();
                                        System.out.print(receiverPrompt + " ");
                                        writer.println(scanner.nextLine());

                                        // Subject
                                        String subjectPrompt = reader.readLine();
                                        System.out.print(subjectPrompt + " ");
                                        writer.println(scanner.nextLine());

                                        // Content
                                        String contentPrompt = reader.readLine();
                                        System.out.print(contentPrompt + " ");
                                        writer.println(scanner.nextLine());

                                        String result = reader.readLine();

                                        System.out.println(result);
                                }

                                // =========================
                                // 2. INBOX
                                // =========================

                                else if ("2".equals(choice)) {

                                        System.out.println();
                                        System.out.println("========== INBOX ==========");

                                        String countLine = reader.readLine();

                                        if (countLine == null) {
                                                System.out.println("Server disconnected.");
                                                break;
                                        }

                                        System.out.println("DEBUG: " + countLine);

                                        if (countLine.startsWith("INBOX_COUNT:")) {

                                                String number = countLine.substring(
                                                                "INBOX_COUNT:".length());

                                                int count = Integer.parseInt(number);

                                                if (count == 0) {

                                                        System.out.println("Inbox is empty.");

                                                } else {

                                                        for (int i = 0; i < count; i++) {

                                                                String emailLine = reader.readLine();

                                                                System.out.println(emailLine);
                                                        }
                                                }

                                                String end = reader.readLine();

                                                if (!"INBOX_END".equals(end)) {
                                                        System.out.println(
                                                                        "DEBUG: " + end);
                                                }

                                        } else {

                                                System.out.println(
                                                                "Server tra ve du lieu khong hop le: "
                                                                                + countLine);
                                        }

                                        System.out.println("============================");
                                }
                                // READ MAIL
                                else if (choice.equals("3")) {

                                        String prompt = reader.readLine();

                                        System.out.print(prompt + " ");

                                        String id = scanner.nextLine();

                                        writer.println(id);

                                        String result = reader.readLine();

                                        if ("EMAIL_FOUND".equals(result)) {

                                                System.out.println();
                                                System.out.println("========== EMAIL ==========");

                                                System.out.println(reader.readLine());
                                                System.out.println(reader.readLine());
                                                System.out.println(reader.readLine());
                                                System.out.println(reader.readLine());

                                                System.out.println("============================");

                                        } else {

                                                System.out.println(result);
                                        }
                                }
                                // SENT MAIL
                                else if ("4".equals(choice)) {

                                        System.out.println();
                                        System.out.println("========== SENT MAIL ==========");

                                        String countLine = reader.readLine();

                                        if (countLine == null) {

                                                System.out.println("Server disconnected.");
                                                break;
                                        }

                                        if (!countLine.startsWith("SENT_COUNT:")) {

                                                System.out.println(
                                                                "Du lieu tu Server khong hop le: "
                                                                                + countLine);

                                                continue;
                                        }

                                        try {

                                                int count = Integer.parseInt(
                                                                countLine.substring(
                                                                                "SENT_COUNT:".length()));

                                                if (count == 0) {

                                                        System.out.println("Sent mail is empty.");

                                                } else {

                                                        for (int i = 0; i < count; i++) {

                                                                System.out.println(
                                                                                reader.readLine());
                                                        }
                                                }

                                                String end = reader.readLine();

                                                if (!"SENT_END".equals(end)) {

                                                        System.out.println(
                                                                        "Warning: " + end);
                                                }

                                        } catch (NumberFormatException e) {

                                                System.out.println(
                                                                "Loi du lieu so tu Server: "
                                                                                + countLine);
                                        }

                                        System.out.println("===============================");
                                }
                                // =========================
                                // 3. LOGOUT
                                // =========================

                                // DELETE MAIL
                                else if ("5".equals(choice)) {

                                        String prompt = reader.readLine();

                                        System.out.print(prompt + " ");

                                        String id = scanner.nextLine();

                                        writer.println(id);

                                        String result = reader.readLine();

                                        if ("DELETE_SUCCESS".equals(result)) {

                                                System.out.println(
                                                                "Email deleted successfully.");

                                        } else {

                                                System.out.println(result);
                                        }
                                }

                                // TRASH
                                else if ("6".equals(choice)) {

                                        System.out.println();
                                        System.out.println("========== TRASH ==========");

                                        String countLine = reader.readLine();

                                        if (countLine == null) {

                                                System.out.println("Server disconnected.");
                                                break;
                                        }

                                        if (!countLine.startsWith("TRASH_COUNT:")) {

                                                System.out.println(
                                                                "Du lieu tu Server khong hop le: "
                                                                                + countLine);

                                                continue;
                                        }

                                        try {

                                                int count = Integer.parseInt(
                                                                countLine.substring(
                                                                                "TRASH_COUNT:".length()));

                                                if (count == 0) {

                                                        System.out.println("Trash is empty.");

                                                } else {

                                                        for (int i = 0; i < count; i++) {

                                                                System.out.println(
                                                                                reader.readLine());
                                                        }
                                                }

                                                String end = reader.readLine();

                                                if (!"TRASH_END".equals(end)) {

                                                        System.out.println(
                                                                        "Warning: " + end);
                                                }

                                        } catch (NumberFormatException e) {

                                                System.out.println(
                                                                "Loi du lieu so tu Server: "
                                                                                + countLine);
                                        }

                                        System.out.println("============================");
                                }

                                else if ("7".equals(choice)) {

                                        String prompt = reader.readLine();

                                        System.out.print(prompt + " ");

                                        String id = scanner.nextLine();

                                        writer.println(id);

                                        String result = reader.readLine();

                                        if ("RESTORE_SUCCESS".equals(result)) {

                                                System.out.println(
                                                                "Email restored successfully.");

                                        } else {

                                                System.out.println(result);
                                        }
                                }

                                else if ("8".equals(choice)) {

                                        String result = reader.readLine();

                                        System.out.println(result);

                                        break;
                                }

                                // =========================
                                // 4. EXIT
                                // =========================

                                else if ("9".equals(choice)) {

                                        String result = reader.readLine();

                                        System.out.println(result);

                                        break;
                                }

                                // =========================
                                // INVALID
                                // =========================

                                else {

                                        String result = reader.readLine();

                                        System.out.println(result);
                                }
                        }

                } catch (Exception e) {

                        System.out.println("Client error:");
                        e.printStackTrace();
                }
        }
}