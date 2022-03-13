package com.example.connecto;

import android.net.Uri;


public class User {

    private String name;
    private String id;

    private String email;
    private Uri image;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Uri getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImage(Uri image) {
        this.image = image;
    }
}
