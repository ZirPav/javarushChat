package com.javarush.task.task30.task3008;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

/*Connection - класс соединения между клиентом и сервером.
* Класс Connection будет выполнять роль обертки над классом java.net.Socket,
которая должна будет уметь сериализовать и десериализовать объекты типа Message в сокет.
*/
public class Connection implements Closeable {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

/*3) Метод void send(Message message) throws IOException.
            Он должен записывать (сериализовать) сообщение message в ObjectOutputStream.
    Этот метод будет вызываться из нескольких потоков.
            Позаботься, чтобы запись в объект ObjectOutputStream была возможна только одним потоком в определенный момент времени,
    остальные желающие ждали завершения записи.
    При этом другие методы класса Connection не должны быть заблокированы.*/

    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    /* 4) Метод Message receive() throws IOException, ClassNotFoundException.
             Он должен читать (десериализовать) данные из ObjectInputStream.
     Сделай так, чтобы операция чтения не могла быть одновременно вызвана несколькими потоками,
     при этом вызов других методы класса Connection не блокировать.*/
    public Message receive() throws IOException, ClassNotFoundException {
        Message message;
        synchronized (in) {
            message = (Message) this.in.readObject();
        }
        return message;
    }

    /*Метод SocketAddress getRemoteSocketAddress(), возвращающий удаленный адрес сокетного соединения.*/

    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    /*должен закрывать все ресурсы класса.*/
    @Override
    public void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }
}
