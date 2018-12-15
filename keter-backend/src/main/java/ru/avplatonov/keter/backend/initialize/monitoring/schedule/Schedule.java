package ru.avplatonov.keter.backend.initialize.monitoring.schedule;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class Schedule {

    private UUID uuidSchedule = UUID.randomUUID();
    private String graphName;
    private String state;
    private String author;
    private Timestamp start;
    private Timestamp end;
    private List<String> tags;

    public UUID getUuidSchedule() {
        return uuidSchedule;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Timestamp getStart() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public Timestamp getEnd() {
        return end;
    }

    public void setEnd(Timestamp end) {
        this.end = end;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Schedule(String graphName, String state, String author, Timestamp start, Timestamp end, List<String> tags) {
        this.graphName = graphName;
        this.state = state;
        this.author = author;
        this.start = start;
        this.end = end;
        this.tags = tags;
    }


}
