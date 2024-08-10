package com.task08;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;


public class OpenMeteoClient {

    public static void main(String[] args) throws Exception {
        System.out.println(new OpenMeteoClient().getWeatherForecast());
    }

    public String getWeatherForecast() throws IOException {
        URL var1 = new URL("https://api.open-meteo.com/v1/forecast?latitude=50.4375&longitude=30.5&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m");
        Scanner var2 = new Scanner((InputStream)var1.getContent());
        StringBuilder var3 = new StringBuilder();

        while(var2.hasNext()) {
            var3.append(var2.nextLine());
        }

        return var3.toString();
    }

}