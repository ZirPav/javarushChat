package com.javarush.task.task30.task3008.client;

/*Клиент, в начале своей работы, должен запросить у пользователя адрес и порт сервера,
подсоединиться к указанному адресу, получить запрос имени от сервера, спросить имя у пользователя,
отправить имя пользователя серверу, дождаться принятия имени сервером.
После этого клиент может обмениваться текстовыми сообщениями с сервером.
Обмен сообщениями будет происходить в двух параллельно работающих потоках.
Один будет заниматься чтением из консоли и отправкой прочитанного серверу,
а второй поток будет получать данные от сервера и выводить их в консоль.
*/

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {

    protected Connection connection;

    /*дальнейшем оно будет устанавливаться в true,
    если клиент подсоединен к серверу или в false в противном случае.*/
    private volatile boolean clientConnected = false;

    /*Он должен создавать вспомогательный поток SocketThread, ожидать
    пока тот установит соединение с сервером, а после этого
    в цикле считывать сообщения с консоли и отправлять их серверу.*/

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true); //обозначили его демоном, чтобы при выходе из программы он сам закрывался
        socketThread.start();

        synchronized (this) {  //Заставить текущий поток ожидать, пока он не получит нотификацию из другого потока.
            try {
                wait();
            } catch (Exception e) {
                ConsoleHelper.writeMessage("Ошибка");
            }
        }

        if (clientConnected) ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        else if (!clientConnected) ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        while (clientConnected) {

            String data = ConsoleHelper.readString();
            if (data.equals("exit")) break;
            if (shouldSendTextFromConsole()) sendTextMessage(data);

        }

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    protected String getServerAddress() {
        /*должен запросить ввод адреса сервера у пользователя и вернуть введенное значение.
                Адрес может быть строкой, содержащей ip, если клиент и сервер запущен на разных машинах или 'localhost', если клиент и сервер работают на одной машине.*/
        ConsoleHelper.writeMessage("Enter server address:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        /*должен запрашивать ввод порта сервера и возвращать его.*/
        ConsoleHelper.writeMessage("Enter server port:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter nameUser:");
        return ConsoleHelper.readString();
        /*должен запрашивать и возвращать имя пользователя.*/
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
        /*в данной реализации клиента всегда должен возвращать true (мы всегда отправляем текст введенный в консоль).
        Этот метод может быть переопределен, если мы будем писать какой-нибудь другой клиент, унаследованный от нашего, который не должен отправлять введенный в консоль текст.*/
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
        /*должен создавать и возвращать новый объект класса SocketThread.*/
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Error");
            clientConnected = false;
        }
        /*создает новое текстовое сообщение, используя переданный текст и отправляет его серверу через соединение connection.
                Если во время отправки произошло исключение IOException, то необходимо вывести информацию об этом пользователю и присвоить false полю clientConnected.*/
    }

    /*отвечает за поток, устанавливающий сокетное соединение и читающий сообщения сервера.*/
    public class SocketThread extends Thread {

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            //должен выводить текст message в консоль.
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " присоединился к беседе.");
            //должен выводить в консоль информацию о том, что участник с именем userName присоединился к чату.
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул беседу.");
            //должен выводить в консоль, что участник с именем userName покинул чат.
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        /*а) Устанавливать значение поля clientConnected внешнего объекта Client в соответствии с переданным параметром.
        б) Оповещать (пробуждать ожидающий) основной поток класса Client.*/

        }

        /*Этот метод будет представлять клиента серверу.*/
        protected void clientHandshake() throws IOException, ClassNotFoundException {
            Message messageIn = null;
            while (true) {
                messageIn = connection.receive();
                //если получили запрос на имя от сервера, то запрашиваем имя и пользователя и пересылаем его серверу
                if (messageIn.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                }
                //если сервер принял имя мы передаем главному потоку(клиенту), что сервер имя принял, погнали
                if (messageIn.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                }
                if (messageIn.getType() != MessageType.NAME_REQUEST && messageIn.getType() != MessageType.NAME_ACCEPTED)
                    throw new IOException("Unexpected MessageType");
            }
        }

        /*Этот метод будет реализовывать главный цикл обработки сообщений сервера*/
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            Message messageLoop;
            while (true) {
                messageLoop = connection.receive();
                if (messageLoop.getType() == MessageType.TEXT) processIncomingMessage(messageLoop.getData());
                if (messageLoop.getType() == MessageType.USER_ADDED) informAboutAddingNewUser(messageLoop.getData());
                if (messageLoop.getType() == MessageType.USER_REMOVED)
                    informAboutDeletingNewUser(messageLoop.getData());
                if (messageLoop.getType() != MessageType.TEXT && messageLoop.getType() != MessageType.USER_ADDED && messageLoop.getType() != MessageType.USER_REMOVED) {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }


/*1. В методе run() должно быть установлено и сохранено в поле connection соединение с сервером
(для получения адреса сервера и порта используй методы getServerAddress() и getServerPort()).
2. В методе run() должен быть вызван метод clientHandshake().
3. В методе run() должен быть вызван метод clientMainLoop().
4. При возникновении исключений IOException или ClassNotFoundException в процессе работы метода run(),
должен быть вызван метод notifyConnectionStatusChanged() с параметром false.*/
        @Override
        public void run() {
            Socket socket = null;
            try {
                socket = new Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }

        }
    }

}
