package com.example.firebase_subida_archivos;

import android.os.Build;
import androidx.annotation.RequiresApi;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class miFile {

    private String nombreFile;
    private String descripcionUser;
    private long messageTime;
    private String nombreUsuario;
    private String fecha;
    private String url;

    public miFile(String nombreText, String descripcionUser,String nombreUsuario,String url) {
        this.nombreFile = nombreText; this.descripcionUser = descripcionUser; this.nombreUsuario = nombreUsuario;

        // Initialize to current time
        messageTime = new Date().getTime();

        this.fecha = this.convertTime(this.messageTime);

        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public miFile(){

    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getDescripcionUser() {
        return descripcionUser;
    }

    public void setDescripcionUser(String descripcionUser) {
        this.descripcionUser = descripcionUser;
    }

    public String getNombreFile() {
        return nombreFile;
    }

    public void setNombreFile(String nombreFile) {
        this.nombreFile = nombreFile;
    }

    public String toString() {
        return this.nombreUsuario + " - " + convertTime(this.messageTime) + "Hs" + "\n" + this.nombreFile +
                " " + this.fecha + " " + this.descripcionUser + "\n" + this.url;
    }

    public String toStringItem() {
        return convertTime(this.messageTime) + "\n" + this.nombreFile + "\n" + this.descripcionUser;
    }

    public String getMessageTimeString() { return convertTime(messageTime); }

    public String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return format.format(date);
    }
}
