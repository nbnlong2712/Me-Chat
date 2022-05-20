package com.longtraidep.appchat.Object;

public class Message {
    String sender;
    String message;
    String name;
    String type;
    String seen = "false";

    public Message(){}

    public Message(String sender, String message, String name, String type, String seen) {
        this.sender = sender;
        this.message = message;
        this.name = name;
        this.type = type;
        this.seen = seen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
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
