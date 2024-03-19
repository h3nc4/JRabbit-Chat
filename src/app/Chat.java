/*
 *  Copyright 2024 Henrique Almeida
 *
 * This file is part of JRabbit Chat.
 *
 * JRabbit Chat is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * JRabbit Chat is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU
 * General Public License along with JRabbit Chat. If not, see
 * <https://www.gnu.org/licenses/>.
*/
package app;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import services.MessageService;

/**
 * Application to call the message service.
 */
public class Chat {
    /** The maximum number of retries. */
    private static int maxretries = 5;
    /** The message service. */
    private final MessageService service;

    /**
     * Initializes the message service.
     *
     * @param host The host of the RabbitMQ server.
     * @param room The room to send and receive messages.
     * @param name The users name.
     * @throws IOException      If an error occurs while creating the connection or
     *                          the channels.
     * @throws TimeoutException If the connection times out.
     */
    public Chat(String host, String room, String name) throws IOException, TimeoutException {
        service = new MessageService(host, room, name);
    }

    /**
     * Runs the application.
     *
     * @throws IOException          If an error occurs while sending or receiving a
     *                              message.
     * @throws InterruptedException If the thread is interrupted while waiting for
     *                              input.
     */
    public void run() throws IOException, InterruptedException {
        new Thread(() -> {
            try {
                service.receiveMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        while (true)
            service.sendMessage(Utils.readStr("Enter message: "));
    }

    /**
     * Main method.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        System.out.println("Welcome to the Chat application");
        if (args.length < 3) {
            System.out.println("Usage: Chat [host] [room] [name]");
            System.exit(1);
        }
        Chat app = null;
        while (app == null) {
            try {
                app = new Chat(args[0], args[1], args[2]);
                app.run();
            } catch (IOException | InterruptedException | TimeoutException e) {
                System.out.println("Error while sending or receiving a message");
            } finally {
                try {
                    Thread.sleep(1000);
                    app.service.close();
                } catch (IOException | TimeoutException | InterruptedException | NullPointerException e) {
                    System.out.println("Error while closing connection");
                }
            }
            app = null;
            if (maxretries-- == 0) {
                System.out.println("Max retries reached. Exiting...");
                break;
            }
            System.out.println("Trying to reconnect...");
        }
    }
}
