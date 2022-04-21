package com.project.tape.SecondaryClasses;

import java.util.ArrayList;

public class JsonData {


    private ArrayList<Playlist> array;

    public JsonData() {
    }

    public JsonData(ArrayList<Playlist> array) {
        this.array = array;
    }

    public ArrayList<Playlist> getArray() {
        return array;
    }

    public void setArray(ArrayList<Playlist> array) {
        this.array = array;
    }
}
