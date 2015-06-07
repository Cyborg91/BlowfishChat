package com.blowfishchat;

import java.io.Serializable;
import java.util.List;

public class Contact implements Serializable {
    private String name;
    private String key;
    private List<String> encryptedMessages;

    public Contact(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getEncryptedMessages() {
        return encryptedMessages;
    }

    public void setEncryptedMessages(List<String> encryptedMessages) {
        this.encryptedMessages = encryptedMessages;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}