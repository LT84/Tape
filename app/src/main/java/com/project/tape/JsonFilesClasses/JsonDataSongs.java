package com.project.tape.JsonFilesClasses;

import com.project.tape.ItemClasses.Song;

import java.util.ArrayList;

public class JsonDataSongs {


    private ArrayList<Song> array;

    public JsonDataSongs() {
    }

    public JsonDataSongs(ArrayList<Song> array) {
        this.array = array;
    }

    public ArrayList<Song> getArray() {
        return array;
    }

    public void setArray(ArrayList<Song> array) {
        this.array = array;
    }
}
