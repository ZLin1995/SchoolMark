package edu.gatech.schoolmark.model;

import java.util.ArrayList;
import java.util.List;



public class Location {

    private String game;
    private List<String> locations;

    public Location() { locations = new ArrayList<>(); }
    public Location(String game, List<String> locations) {
        this.game = game;
        this.locations = locations;
        if (this.locations == null) {
            this.locations = new ArrayList<>();
        }
    }

    public String getGame() { return game; }

    public List<String> getLocations() { return locations; }

    public void setLocations(List<String> locations) { this.locations = locations; }

    public String toString() { return game; }

    public boolean equals(Object o) {
//        if (o instanceof String) {
//            return (o.equals(game));
//        }
        Location s = (Location) o;
        return (s.getGame().equals(game) && s.getLocations().equals(locations));
    }

    public boolean equals(String s) {
        return s.equals(game);
    }

}
