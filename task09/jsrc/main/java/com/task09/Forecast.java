package com.task09;

public class Forecast {
    private Number latitude;

    public Number getLatitude() {
        return latitude;
    }

    public void setLatitude(Number latitude) {
        this.latitude = latitude;
    }

    public Number getLongitude() {
        return longitude;
    }

    public void setLongitude(Number longitude) {
        this.longitude = longitude;
    }

    public Number getGenerationtime_ms() {
        return generationtime_ms;
    }

    public void setGenerationtime_ms(Number generationtime_ms) {
        this.generationtime_ms = generationtime_ms;
    }

    public Number getUtc_offset_seconds() {
        return utc_offset_seconds;
    }

    public void setUtc_offset_seconds(Number utc_offset_seconds) {
        this.utc_offset_seconds = utc_offset_seconds;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimezone_abbreviation() {
        return timezone_abbreviation;
    }

    public void setTimezone_abbreviation(String timezone_abbreviation) {
        this.timezone_abbreviation = timezone_abbreviation;
    }

    public Number getElevation() {
        return elevation;
    }

    public void setElevation(Number elevation) {
        this.elevation = elevation;
    }

    public HourlyUnits getHourly_units() {
        return hourly_units;
    }

    public void setHourly_units(HourlyUnits hourly_units) {
        this.hourly_units = hourly_units;
    }

    public Hourly getHourly() {
        return hourly;
    }

    public void setHourly(Hourly hourly) {
        this.hourly = hourly;
    }

    private Number longitude;
    private Number generationtime_ms;
    private Number utc_offset_seconds;
    private String timezone;
    private String timezone_abbreviation;
    private Number elevation;
    private HourlyUnits hourly_units;
    private Hourly hourly;

}