package com.longtraidep.appchat.Object;

import java.io.Serializable;

public class Users implements Serializable {

    private String Id;
    private String Username;
    private String Img;
    private String State = "false";
    private String Token;

    public Users() {
    }

    public Users(String id, String username, String img, String state, String token) {
        Id = id;
        Username = username;
        Img = img;
        State = state;
        Token = token;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getImg() {
        return Img;
    }

    public void setImg(String img) {
        Img = img;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }
}
