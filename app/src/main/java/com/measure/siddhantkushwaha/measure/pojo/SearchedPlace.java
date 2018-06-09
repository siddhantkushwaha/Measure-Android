package com.measure.siddhantkushwaha.measure.pojo;

public class SearchedPlace {

    private String description;
    private String place_id;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getPlace_id() {
        return place_id;
    }

    public String toString() {
        return getDescription();
    }
}
