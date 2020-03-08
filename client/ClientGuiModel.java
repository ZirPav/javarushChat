package com.javarush.task.task30.task3008.client;

import javax.swing.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientGuiModel {


    /*В нем будет храниться список всех участников чата.*/
    private final Set<String> allUserNames = new HashSet<>();
    private String newMessage;

    public Set<String> getAllUserNames() {
        Set<String> stringSet = Collections.unmodifiableSet(allUserNames);
        return stringSet;
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public void addUser(String newUserName){
        allUserNames.add(newUserName);
    }

    public void deleteUser(String userName){
        allUserNames.remove(userName);

    }
}
