package edu.gatech.schoolmark.model;

import java.util.ArrayList;
import java.util.List;



public class Sports {
    private List<String> sports;

    public Sports() {
        this.sports = new ArrayList<>();
    }

    public void setSports(List<String> sports) { this.sports = sports; }
    public void add(String sport) { sports.add(sport); }
    public List<String> getSports() { return sports; }
}
