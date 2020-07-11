package edu.gatech.schoolmark.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Armando on 9/25/17.
 * List of available Sports in the Database
 * This list should be obtained from Firebase and used to get SportsLocations
 */

public class Sports {
    private List<String> sports;

    public Sports() {
        this.sports = new ArrayList<>();
    }

    public void setSports(List<String> sports) { this.sports = sports; }
    public void add(String sport) { sports.add(sport); }
    public List<String> getSports() { return sports; }
}
