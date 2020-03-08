package com.javarush.task.task30.task3008;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server - основной класс сервера.
 * Сервер должен поддерживать множество соединений с разными клиентами одновременно.
 * Это можно реализовать с помощью следующего алгоритма:
 * <p>
 * - Сервер создает серверное сокетное соединение.
 * - В цикле ожидает, когда какой-то клиент подключится к сокету.
 * - Создает новый поток обработчик Handler, в котором будет происходить обмен сообщениями с клиентом.
 * - Ожидает следующее соединение.
 */
public class Server {

    /*Т.к. сервер может одновременно работать с несколькими клиентами,
нам понадобится метод для отправки сообщения сразу всем.
где ключом будет имя клиента, а значением - соединение с ним.*/
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Создаем сервер, вводим номер порта:");
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt());) {
            ConsoleHelper.writeMessage("Cервер запущен.");

            while (true) {
                Socket socket = serverSocket.accept(); //ожидания подключения
                System.err.println("Client accepted");
                //стартуем обработку клиента в отдельном потоке
                new Handler(socket).start();
            }
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }
    }

    /*Статический метод void sendBroadcastMessage(Message message),
    который должен отправлять сообщение message всем соединениям из connectionMap.
    Если при отправке сообщение произойдет исключение IOException,
    нужно отловить его и сообщить пользователю, что не смогли отправить сообщение.*/

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> map : connectionMap.entrySet()) {
            try {
                map.getValue().send(message);
            } catch (IOException e) {
                e.getMessage();
            }
        }
    }

    /**
     * Класс Handler должен реализовывать протокол общения с клиентом.
     * Выделим из протокола отдельные этапы и реализуем их с помощью отдельных методов:
     */

    private static class Handler extends Thread {

        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }


   /* Этап первый - это этап рукопожатия (знакомства сервера с клиентом).
     Метод в качестве параметра принимает соединение connection, а возвращает имя нового клиента.*/
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message answer = connection.receive();

                if (answer.getType() == MessageType.USER_NAME) {

                    if (!answer.getData().isEmpty()) {
                        if (!connectionMap.containsKey(answer.getData())) {
                            connectionMap.put(answer.getData(), connection);
                            connection.send(new Message(MessageType.NAME_ACCEPTED));
                            return answer.getData();
                        }
                    }
                }
            }
        }

        /*connection - соединение с участником, которому будем слать информацию, а userName - его имя.*/
        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> map:connectionMap.entrySet()) {
                if (!map.getKey().equals(userName))
                    connection.send(new Message(MessageType.USER_ADDED, map.getKey()));
            }
        }

        /*пользователь отправляет текстовое сообщение другим участникам "чата"*/
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true){
                Message textIn = connection.receive();
                if (textIn.getType() == MessageType.TEXT){
                    String sendText = userName + ":" + " " + textIn.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, sendText));
                }else if (textIn.getType() != MessageType.TEXT){
                    ConsoleHelper.writeMessage("Сообщение не является текстом");
                }

            }
        }


        /*Пришло время написать главный метод класса Handler,
        который будет вызывать все вспомогательные методы, написанные ранее.*/
        @Override
        public void run() {
            String nameUser;
            /*Выводить сообщение, что установлено новое соединение с удаленным адресом,
            который можно получить с помощью метода getRemoteSocketAddress().*/
            ConsoleHelper.writeMessage("Соединение установлено " + socket.getRemoteSocketAddress());
            try(Connection connection = new Connection(socket)) {

                    nameUser = serverHandshake(connection); // получаем имя пользователя

                    /*отправляем сообщение всем connection,
                    что такой пользователь добавлен*/
                    sendBroadcastMessage(new Message(MessageType.USER_ADDED, nameUser));
                    notifyUsers(connection, nameUser);//Сообщать новому участнику о существующих участниках
                    serverMainLoop(connection, nameUser); //передача сообщения
                    connectionMap.remove(nameUser);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED,nameUser));
            } catch (ClassNotFoundException | IOException e) {
                ConsoleHelper.writeMessage("Ошибка " + e);
            }


        }
    }
}



/**Чат (22)
 Итак, подведем итог:
 • Ты написал сервер для обмена текстовыми сообщениями.
 • Ты написал консольный клиент, который умеет подключаться к серверу и обмениваться сообщениями с другими участниками.
 • Ты написал бот клиента, который может принимать запросы и отправлять данные о текущей дате и времени.
 • Ты написал клиента для чата с графическим интерфейсом.

 Что можно добавить или улучшить:
 • Можно добавить поддержку приватных сообщений (когда сообщение отправляется не всем, а какому-то конкретному участнику).
 • Можно расширить возможности бота, попробовать научить его отвечать на простейшие вопросы или время от времени отправлять шутки.
 • Добавить возможность пересылки файлов между пользователями.
 • Добавить контекстное меню в графический клиент, например, для отправки приватного сообщения кому-то из списка участников.
 • Добавить раскраску сообщений в графическом клиенте в зависимости от отправителя.
 • Добавить блокировку сервером участников за что-либо, например, ненормативную лексику в сообщениях.
 • Добавить еще миллион фич и полезностей!

 Ты научился:
 • Работать с сокетами.
 • Пользоваться сериализацией и десериализацией.
 • Создавать многопоточные приложения, синхронизировать их, применять модификатор volatile, пользоваться классами из библиотеки java.util.concurrent.
 • Применять паттерн MVC.
 • Использовать внутренние и вложенные классы.
 • Работать с библиотекой Swing.
 • Применять классы Calendar и SimpleDateFormat.

 Так держать!


 Требования:
 1. Поздравляю, чат готов!*/