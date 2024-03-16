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
package services;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import app.Chat;

/**
 * Class to send and receive messages using RabbitMQ.
 */
public class MessageService {
    /** The connection to the RabbitMQ server. */
    private final Connection connection;
    /** The channel to send messages. */
    private final Channel sendChannel;
    /** The channel to receive messages. */
    private final Channel receiveChannel;

    /**
     * Initializes the connection to the RabbitMQ server and the channels to send
     * and receive messages.
     *
     * @throws IOException      If an error occurs while creating the connection or
     *                          the channels.
     * @throws TimeoutException If the connection times out.
     */
    public MessageService() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Chat.HOST);
        connection = factory.newConnection();
        sendChannel = connection.createChannel();
        receiveChannel = connection.createChannel();
        sendChannel.exchangeDeclare(Chat.EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
    }

    /**
     * Sends a message to the exchange.
     *
     * @param message The message to send.
     * @throws IOException If an error occurs while sending the message.
     */
    public void sendMessage(String message) throws IOException {
        sendChannel.basicPublish(Chat.EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
    }

    /**
     * Receives a message from the exchange.
     *
     * @throws IOException If an error occurs while receiving the message.
     */
    public void receiveMessage() throws IOException {
        String queueName = receiveChannel.queueDeclare().getQueue();
        receiveChannel.queueBind(queueName, Chat.EXCHANGE_NAME, "");
        DefaultConsumer consumer = new DefaultConsumer(receiveChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                    byte[] body) throws IOException {
                System.out.printf("\nReceived: %s\nEnter message: ", new String(body, "UTF-8"));
            }
        };
        receiveChannel.basicConsume(queueName, true, consumer);
    }

    /**
     * Closes the connection to the RabbitMQ server and the channels to send and
     * receive messages.
     *
     * @throws IOException      If an error occurs while closing the connection or
     *                          the channels.
     * @throws TimeoutException If the connection times out.
     */
    public void close() throws IOException, TimeoutException {
        sendChannel.close();
        receiveChannel.close();
        connection.close();
    }
}
