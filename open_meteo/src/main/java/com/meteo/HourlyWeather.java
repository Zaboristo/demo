package com.meteo;

import java.util.List;

public class HourlyWeather {
    private List<String> time;
    private List<Double> temperature2m;
    private List<Integer> relativeHumidity2m;
    private List<Double> windSpeed10m;

    // Default constructor
    public HourlyWeather() {}

    // Parameterized constructor
    public HourlyWeather(List<String> time, List<Double> temperature2m, List<Integer> relativeHumidity2m,
                         List<Double> windSpeed10m) {
        this.time = time;
        this.temperature2m = temperature2m;
        this.relativeHumidity2m = relativeHumidity2m;
        this.windSpeed10m = windSpeed10m;
    }

    // Getters and setters
    public List<String> getTime() {
        return time;
    }

    public void setTime(List<String> time) {
        this.time = time;
    }

    public List<Double> getTemperature2m() {
        return temperature2m;
    }

    public void setTemperature2m(List<Double> temperature2m) {
        this.temperature2m = temperature2m;
    }

    public List<Integer> getRelativeHumidity2m() {
        return relativeHumidity2m;
    }

    public void setRelativeHumidity2m(List<Integer> relativeHumidity2m) {
        this.relativeHumidity2m = relativeHumidity2m;
    }

    public List<Double> getWindSpeed10m() {
        return windSpeed10m;
    }

    public void setWindSpeed10m(List<Double> windSpeed10m) {
        this.windSpeed10m = windSpeed10m;
    }
}