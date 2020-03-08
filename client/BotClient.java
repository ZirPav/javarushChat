package com.javarush.task.task30.task3008.client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;


public class BotClient extends Client {

    @Override
    protected String getUserName() {
        int x = (int) (Math.random() * 100);
        String nameBot = "date_bot_";
        return nameBot + x;
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }


    public class BotSocketThread extends SocketThread {

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);
            String[] name = null;
            String nameUser = null;
            if (message.contains(":")) {
                name = message.split(":");
                nameUser = name[0];

                SimpleDateFormat dateFormat = new SimpleDateFormat();

                switch (name[1].trim()) {
                    case "дата":
                        dateFormat.applyPattern("d.MM.YYYY");
                        sendTextMessage(String.format("Информация для %s: %s", nameUser, dateFormat.format(Calendar.getInstance().getTime())));
                        break;
                    case "день":
                        dateFormat.applyPattern("d");
                        sendTextMessage(String.format("Информация для %s: %s", nameUser, dateFormat.format(Calendar.getInstance().getTime())));
                        break;
                    case "месяц":
                        dateFormat.applyPattern("MMMM");
                        sendTextMessage(String.format("Информация для %s: %s", nameUser, dateFormat.format(Calendar.getInstance().getTime())));
                        break;
                    case "год":
                        dateFormat.applyPattern("YYYY");
                        sendTextMessage(String.format("Информация для %s: %s", nameUser, dateFormat.format(Calendar.getInstance().getTime())));
                        break;
                    case "время":
                        dateFormat.applyPattern("H:mm:ss");
                        sendTextMessage(String.format("Информация для %s: %s", nameUser, dateFormat.format(Calendar.getInstance().getTime())));
                        break;
                    case "час":
                        dateFormat.applyPattern("H");
                        sendTextMessage(String.format("Информация для %s: %s", nameUser, dateFormat.format(System.currentTimeMillis())));
                        break;
                    case "минуты":
                        dateFormat.applyPattern("m");
                        sendTextMessage(String.format("Информация для %s: %s", nameUser, dateFormat.format(Calendar.getInstance().getTime())));
                        break;
                    case "секунды":
                        dateFormat.applyPattern("s");
                        sendTextMessage(String.format("Информация для %s: %s", nameUser, dateFormat.format(Calendar.getInstance().getTime())));
                        break;
                }
            }
        }
    }
}
