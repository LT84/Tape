package com.project.tape.JsonFilesClasses;

import com.project.tape.ItemClasses.Playlist;

import java.util.ArrayList;


public class JsonDataPlaylists {


    private ArrayList<Playlist> array;

    public JsonDataPlaylists() {
    }

    public JsonDataPlaylists(ArrayList<Playlist> array) {
        this.array = array;
    }

    public ArrayList<Playlist> getArray() {
        return array;
    }

    public void setArray(ArrayList<Playlist> array) {
        this.array = array;
    }
}
