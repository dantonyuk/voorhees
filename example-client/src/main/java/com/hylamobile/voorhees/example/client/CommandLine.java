package com.hylamobile.voorhees.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class CommandLine {

    interface CommandHandler {
        void handle(String command, String argument);
    }

    void run(CommandHandler handler) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            line = line.trim();
            String[] parsed = line.split("\\s+", 2);

            String command = parsed.length > 0 ? parsed[0] : "";
            if ("exit".equals(command)) {
                System.exit(0);
            }

            String commandArg = parsed.length > 1 ? parsed[1] : "";
            handler.handle(command, commandArg);
        }
    }
}
