package com.meteo;

public class CurrentWeather {
    private String time;
    private int interval;
    private double temperature2m;
    private double windSpeed10m;

    // Getters and setters
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public double getTemperature2m() {
        return temperature2m;
    }

    public void setTemperature2m(double temperature2m) {
        this.temperature2m = temperature2m;
    }

    public double getWindSpeed10m() {
        return windSpeed10m;
    }

    public void setWindSpeed10m(double windSpeed10m) {
        this.windSpeed10m = windSpeed10m;
    }

    public CurrentWeather(String time, int interval, double temperature2m, double windSpeed10m) {
        this.time = time;
        this.interval = interval;
        this.temperature2m = temperature2m;
        this.windSpeed10m = windSpeed10m;
    }
}