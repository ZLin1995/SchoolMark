package edu.gatech.schoolmark.model;

import java.util.ArrayList;
import java.util.List;



public class EventsLocations {

    private String event;
    private List<String> locations;

    public EventsLocations() { locations = new ArrayList<>(); }
    public EventsLocations(String event, List<String> locations) {
        this.event = event;
        this.locations = locations;
        if (this.locations == null) {
            this.locations = new ArrayList<>();
        }
    }

    public String getEvent() { return event; }

    public List<String> getLocations() { return locations; }

    public void setLocations(List<String> locations) { this.locations = locations; }

    public String toString() { return event; }

    public boolean equals(Object o) {
        EventsLocations s = (EventsLocations) o;
        return (s.getEvent().equals(event) && s.getLocations().equals(locations));
    }

    public boolean equals(String s) {
        return s.equals(event);
    }

}
