package com.project.tape.JsonFilesClasses;

import java.util.Map;

public class JsonDataMap {

    private Map<String, String> map;

    public JsonDataMap() {
    }

    public JsonDataMap(Map<String, String> map) {
        this.map = map;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }


}
