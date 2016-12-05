package com.example.chinhnb.placemap.Entity;

/**
 * Created by CHINHNB on 11/16/2016.
 */

public class Localtion {
    private String title, genre, year;

    public Localtion() {
    }

    public Localtion(String title, String genre, String year) {
        this.title = title;
        this.genre = genre;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
