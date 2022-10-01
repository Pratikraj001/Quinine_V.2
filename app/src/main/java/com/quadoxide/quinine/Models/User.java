package com.quadoxide.quinine.Models;

public class User {

    public String user_id, name, email, photo;
    public User(){}

    public User(String user_id, String name, String email, String photo) {
        this.user_id = user_id;
        this.name = name;
        this.email = email;
        this.photo = photo;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
