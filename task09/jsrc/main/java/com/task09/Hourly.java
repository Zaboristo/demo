package com.task09;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties
public class Hourly {
    private List<String> time;

    public List<String> getTime() {
        return time;
    }

    public void setTime(List<String> time) {
        this.time = time;
    }

    public List<Number> getTemperature_2m() {
        return temperature_2m;
    }

    public void setTemperature_2m(List<Number> temperature_2m) {
        this.temperature_2m = temperature_2m;
    }

    private List<Number> temperature_2m;
}