package com.blowfishchat;


import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private String nadawca;
    private String odbiorca;
    private String tresc;
    private Date data;

    public Message() {
    }

    public Message(String nadawca, String odbiorca, String tresc) {
        this.nadawca = nadawca;
        this.odbiorca = odbiorca;
        this.tresc = tresc;
        this.data = new Date();
    }

    public String getNadawca() {
        return nadawca;
    }

    public void setNadawca(String nadawca) {
        this.nadawca = nadawca;
    }

    public String getOdbiorca() {
        return odbiorca;
    }

    public void setOdbiorca(String odbiorca) {
        this.odbiorca = odbiorca;
    }

    public String getTresc() {
        return tresc;
    }

    public void setTresc(String tresc) {
        this.tresc = tresc;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }
}
