package com.project.tape.ItemClasses;


public class Song  {

    private String data;
    private String title;
    private String album;
    private String artist;
    private String duration;
    private boolean isSelected;


    public Song(String data, String title, String album, String artist, String duration, boolean isSelected) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.isSelected = isSelected;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDuration() {
        return duration;
    }

    public boolean getSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


}



