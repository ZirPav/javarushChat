package com.javarush.task.task30.task3008;

import java.io.Serializable;

/*Message - класс, отвечающий за пересылаемые сообщения.*/
public class Message implements Serializable {

    private final MessageType type;                 //тип сообщения
    private final String data;                      //данные о сообщении(у нас тут имя юзера)

    public Message(MessageType type) {
        this.type = type;
        this.data = null;
    }

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }


    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
