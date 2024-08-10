package com.meteo;

public class Units {
    private String time;
    private String interval;
    private String temperature2m;
    private String windSpeed10m;

    // Getters and setters
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getTemperature2m() {
        return temperature2m;
    }

    public void setTemperature2m(String temperature2m) {
        this.temperature2m = temperature2m;
    }

    public String getWindSpeed10m() {
        return windSpeed10m;
    }

    public void setWindSpeed10m(String windSpeed10m) {
        this.windSpeed10m = windSpeed10m;
    }


    // Default constructor
    public Units() {}

    // Parameterized constructor
    public Units(String time, String interval, String temperature2m, String windSpeed10m) {
        this.time = time;
        this.interval = interval;
        this.temperature2m = temperature2m;
        this.windSpeed10m = windSpeed10m;
    }
}