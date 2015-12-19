package com.mycompany.normalizerjsonbank;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
/**
 *
 * @author Thomas Kragsberger
 */
public class NormalizerJSONBank {
    public static ConnectionFactory getConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        factory.setPort(5672);
        factory.setUsername("student");
        factory.setPassword("cph");
        return factory;
    }
    
    public static String normalizeXML(String message){
        String[] token = message.split("[:,}]");
        String result = token[3]+">>>"+token[1];
        return result;
    }
    
    private final static String REPLY_QUEUE_NAME = "group10.replyChannel.bankJSON";
    private final static String QUEUE_NAME = "group10.Aggregator";

    public static void main(String[] argv) throws Exception {
        Connection connection = getConnection().newConnection();
        Channel channel = connection.createChannel();

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        channel.queueDeclare(REPLY_QUEUE_NAME, false, false, false, null);
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                message = normalizeXML(message);
                System.out.println(" [x] Result '" + message + "'");
                Connection connection2 = getConnection().newConnection();
                Channel channel2 = connection2.createChannel();
                channel2.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel2.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
                channel2.close();
                connection2.close();
            }
        };
        channel.basicConsume(REPLY_QUEUE_NAME, true, consumer);
    }
}
