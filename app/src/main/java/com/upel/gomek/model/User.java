package com.upel.gomek.model;

public class User {
    private String email,password,nama,telp,jamBuka,daftarMekanik,avatarUrl, rate;

    public User(){
    }

    public User(String email, String password, String nama, String telp, String jamBuka, String daftarMekanik, String avatarUrl, String rate){
        this.email = email;
        this.password = password;
        this.nama = nama;
        this.telp = telp;
        this.jamBuka = jamBuka;
        this.daftarMekanik = daftarMekanik;
        this.avatarUrl = avatarUrl;
        this.rate = rate;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
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

    public String getJamBuka() {
        return jamBuka;
    }

    public void setJamBuka(String jamBuka) {
        this.jamBuka = jamBuka;
    }

    public String getDaftarMekanik() {
        return daftarMekanik;
    }

    public void setDaftarMekanik(String daftarMekanik) {
        this.daftarMekanik = daftarMekanik;
    }
}
