package com.upel.gomek.model;

public class Cust {
    private String email,password,nama,telp,avatarUrl;

    public Cust(){
    }

    public Cust(String email, String password, String nama, String telp, String avatarUrl){
        this.email = email;
        this.password = password;
        this.nama = nama;
        this.telp = telp;
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getTelp() {
        return telp;
    }

    public void setTelp(String telp) {
        this.telp = telp;
    }

}
