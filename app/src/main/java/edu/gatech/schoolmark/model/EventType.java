package edu.gatech.schoolmark.model;

import java.util.ArrayList;
import java.util.List;



public class EventType {
    private List<String> types;

    public EventType() {
        this.types = new ArrayList<>();
    }

    public void setEventTypes(List<String> types) { this.types = types; }
    public void add(String type) { types.add(type); }
    public List<String> getTypes() { return types; }
}
