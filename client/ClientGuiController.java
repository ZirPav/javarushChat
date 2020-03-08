package com.javarush.task.task30.task3008.client;

public class ClientGuiController extends Client {

    private ClientGuiModel model = new ClientGuiModel();
    private ClientGuiView view = new ClientGuiView(this);


    /*должен получать объект SocketThread через метод getSocketThread() и вызывать у него метод run().
Разберись, почему нет необходимости вызывать метод run() в отдельном потоке, как мы это делали для консольного клиента.*/
    @Override
    public void run() {
       getSocketThread().run();
    }


    //getServerAddress(), getServerPort(), getUserName().- Они должны вызывать одноименные методы из представления (view).
    @Override
    protected String getServerAddress() {
        return view.getServerAddress();
    }

    @Override
    protected int getServerPort() {
        return view.getServerPort();
    }

    @Override
    protected String getUserName() {
        return view.getUserName();
    }

    /*должен создавать и возвращать объект типа GuiSocketThread.*/
    @Override
    protected SocketThread getSocketThread() {
        return new GuiSocketThread();
    }

    /*который должен возвращать модель*/
    public ClientGuiModel getModel(){
        return model;
    }

    /*который должен создавать новый объект ClientGuiController и вызывать у него метод run()*/
    public static void main(String[] args) {
        ClientGuiController clientGuiController = new ClientGuiController();
        clientGuiController.run();
    }


    public class GuiSocketThread extends SocketThread{

        /*должен устанавливать новое сообщение у модели и вызывать обновление вывода сообщений у представления.*/
        @Override
        protected void processIncomingMessage(String message) {
            model.setNewMessage(message);
            view.refreshMessages();
        }

        /*должен добавлять нового пользователя в модель и вызывать обновление вывода пользователей у отображения.*/
        @Override
        protected void informAboutAddingNewUser(String userName) {
            model.addUser(userName);
            view.refreshUsers();
        }

        /*должен удалять пользователя из модели и вызывать обновление вывода пользователей у отображения.*/
        @Override
        protected void informAboutDeletingNewUser(String userName) {
            model.deleteUser(userName);
            view.refreshUsers();
        }

        /*должен вызывать аналогичный метод у представления.*/
        @Override
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            view.notifyConnectionStatusChanged(clientConnected);
        }
    }

}


