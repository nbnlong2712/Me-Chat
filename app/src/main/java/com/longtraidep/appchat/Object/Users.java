package com.longtraidep.appchat.Object;

import java.io.Serializable;

public class Users implements Serializable {

    private String Id;
    private String Username;
    private String Img;

    public Users() {
    }

    public Users(String id, String username, String img) {
        Id = id;
        Username = username;
        Img = img;
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
}
