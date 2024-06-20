package org.example;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Scanner;
import java.util.UUID;

public class ChatClient {
    private static final String TOPIC_NAME = "topic1"; // Nazwa tematu do komunikacji

    private String clientId; // Unikalne ID klienta
    private Connection connection; // Połączenie z brokerem JMS
    private Session session; // Sesja JMS
    private MessageProducer producer; // Producent wiadomości
    private MessageConsumer consumer; // Konsument wiadomości

    public ChatClient() throws JMSException, NamingException {
        clientId = UUID.randomUUID().toString(); // Generowanie unikalnego ID klienta

        // Inicjalizacja kontekstu JNDI
        Context ctx = new InitialContext();

        // Pobranie fabryki połączeń
        ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
        connection = connectionFactory.createConnection("admin", "admin"); // Tworzenie połączenia z podaniem loginu i hasła
        connection.setClientID(clientId); // Ustawienie unikalnego ID klienta
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // Tworzenie sesji JMS bez transakcji, z automatycznym potwierdzeniem

        // Pobranie tematu
        Topic topic = (Topic) ctx.lookup(TOPIC_NAME);

        // Tworzenie producenta wiadomości dla danego tematu
        producer = session.createProducer(topic);
        // Tworzenie konsumenta wiadomości dla danego tematu
        consumer = session.createConsumer(topic);

        // Ustawienie nasłuchiwacza wiadomości
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    if (message instanceof TextMessage) { // Sprawdzenie, czy wiadomość jest typu TextMessage
                        TextMessage textMessage = (TextMessage) message;
                        System.out.println(textMessage.getText()); // Wyświetlenie treści wiadomości
                    }
                } catch (JMSException e) {
                    e.printStackTrace(); // Obsługa wyjątku
                }
            }
        });

        connection.start(); // Uruchomienie połączenia, aby zaczęło odbierać wiadomości
    }

    public void sendMessage(String messageText) throws JMSException {
        String message = clientId + ": " + messageText; // Dodanie ID klienta do treści wiadomości
        TextMessage textMessage = session.createTextMessage(message); // Tworzenie wiadomości tekstowej
        producer.send(textMessage); // Wysłanie wiadomości
    }

    public void close() throws JMSException {
        connection.close(); // Zamknięcie połączenia
    }

    public static void main(String[] args) {
        try {
            ChatClient chatClient = new ChatClient(); // Tworzenie instancji ChatClient
            Scanner scanner = new Scanner(System.in); // Inicjalizacja skanera do odczytu danych z konsoli
            System.out.println("Enter your messages:");

            while (true) { // Pętla nieskończona do wysyłania wiadomości
                String message = scanner.nextLine(); // Odczytanie wiadomości z konsoli
                chatClient.sendMessage(message); // Wysłanie wiadomości
            }
        } catch (JMSException | NamingException e) {
            e.printStackTrace(); // Obsługa wyjątków JMSException i NamingException
        }
    }
}
