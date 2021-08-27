package com.longtraidep.appchat.Object;

public class Message {
    String sender;
    String message;
    String type;
    Boolean isSeen;

    public Message(){}

    public Message(String sender, String message, String type, Boolean isSeen) {
        this.sender = sender;
        this.message = message;
        this.type = type;
        this.isSeen = isSeen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
