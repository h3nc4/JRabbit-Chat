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
import java.util.Collections;
import java.util.UUID;

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
    /** The id of the sender. */
    private final String senderId;
    /** The room to send and receive messages. */
    private final String exchange;
    /** The users name. */
    private final String username;

    /**
     * Initializes the connection to the RabbitMQ server and the channels to send
     * and receive messages.
     *
     * @param host     The host of the RabbitMQ server.
     * @param exchange The room to send and receive messages.
     * @param username The users name.
     * @throws IOException      If an error occurs while creating the connection or
     *                          the channels.
     * @throws TimeoutException If the connection times out.
     */
    public MessageService(String host, String exchange, String username) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        connection = factory.newConnection();
        sendChannel = connection.createChannel();
        receiveChannel = connection.createChannel();
        senderId = UUID.randomUUID().toString();
        this.exchange = exchange;
        this.username = username;
        sendChannel.exchangeDeclare(exchange, BuiltinExchangeType.TOPIC, true);
    }

    /**
     * Sends a message to the exchange.
     *
     * @param message The message to send.
     * @throws IOException If an error occurs while sending the message.
     */
    public void sendMessage(String message) throws IOException {
        sendChannel.basicPublish(exchange, "",
                new AMQP.BasicProperties.Builder().headers(Collections.singletonMap("SenderId", senderId)).build(),
                String.format("%s: %s", username, message).getBytes("UTF-8"));
    }

    /**
     * Receives a message from the exchange.
     *
     * @throws IOException If an error occurs while receiving the message.
     */
    public void receiveMessage() throws IOException {
        String queueName = receiveChannel.queueDeclare().getQueue();
        receiveChannel.queueBind(queueName, exchange, "");
        DefaultConsumer consumer = new DefaultConsumer(receiveChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (!senderId.equals(properties.getHeaders().get("SenderId").toString()))
                    System.out.printf("\n%s\nEnter message: ", new String(body, "UTF-8"));
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
