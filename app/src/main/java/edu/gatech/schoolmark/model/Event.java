package edu.gatech.schoolmark.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class Event {

    private String event;
    private Date timeOfEvent;
    private String locationTitle;
    private int capacity;
    private String eventStatus;
    private String hostUID;
    private List<String> playerUIDList;
    private String name;
    private String description;


    private boolean isExclusive;

    private boolean isHostStudent;

    public Event() {
        this.event = "";
        this.name = "";
        this.timeOfEvent = new Date();
        this.locationTitle = "";
        this.capacity = 0;
        this.eventStatus = "";
        this.hostUID = "";
        this.playerUIDList = new ArrayList<>();
        this.isExclusive = false;
        this.description = "";
    }

    public Event(String event, Date timeOfGame, String locationTitle, int capacity , String gameStatus, String hostUID, ArrayList<String> playerUIDList, boolean isExclusive, String name, String description) {
        this.event = event;
        this.name = name;
        this.timeOfEvent = timeOfGame;
        this.locationTitle = locationTitle;
        this.capacity = capacity;
        this.eventStatus = gameStatus;
        this.hostUID = hostUID;
        this.playerUIDList = playerUIDList;
        this.isExclusive = isExclusive;
        this.description = description;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Date getTimeOfEvent() {
        return timeOfEvent;
    }

    public void setTimeOfEvent(Date timeOfGame) {
        this.timeOfEvent = timeOfEvent;
    }

    public String getEventName() {
        return name;
    }

    public void setEventName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationTitle() {
        return locationTitle;
    }

    public void setLocationTitle(String locationTitle) {
        this.locationTitle = locationTitle;
    }

    public int getCapacity() { return capacity; }

    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getEventStatus() {
        return eventStatus;
    }


    public String getHostUID() {
        return hostUID;
    }

    public void setHostUID(String hostUID) {
        this.hostUID = hostUID;
    }

    public List<String> getPlayerUIDList() {
        return playerUIDList;
    }

    public void setPlayerUIDList(ArrayList<String> playerUIDList) {
        this.playerUIDList = playerUIDList;
    }

    public boolean getIsExclusive() {
        return isExclusive;
    }

    public void setIsExclusive(boolean isHostStudent) {
        this.isExclusive = isHostStudent;
    }

    public boolean getIsHostStudent() {
        return isHostStudent;
    }

    public void setIsHostStudent(boolean isHostStudent) {
        this.isHostStudent = isHostStudent;
    }



    @Override
    public String toString() {
        return locationTitle + " " + event + " " + hostUID;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Event)) { return false; }
        Event g = (Event) o;

        if (event.equals(g.getEvent())
                && timeOfEvent.equals(g.getTimeOfEvent())
                && locationTitle.equals(g.getLocationTitle())
                && capacity == g.getCapacity()
                && eventStatus.equals(g.getEventStatus())
                && hostUID.equals(g.getHostUID())
                && playerUIDList.equals(g.getPlayerUIDList())) { return true; }
        return false;
    }
}
