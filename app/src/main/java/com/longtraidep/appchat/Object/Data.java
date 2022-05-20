package com.longtraidep.appchat.Object;

public class Data {
    private String name;
    private String message;
    private String img;

    public Data(String name, String message, String img) {
        this.name = name;
        this.message = message;
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
