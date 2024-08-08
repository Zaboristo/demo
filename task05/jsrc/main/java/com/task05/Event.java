package com.task05;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Event {
    private String id;
    private int principalId;
    private String createdAt;
    private Map<String, String> body;

    public Event() {}

    public Event(int principalId, Map<String, String> body) {
        this.id = UUID.randomUUID().toString();
        this.principalId = principalId;
        this.createdAt = Instant.now().toString();
        this.body = body;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, String> getBody() {
        return body;
    }

    public void setBody(Map<String, String> body) {
        this.body = body;
    }
}