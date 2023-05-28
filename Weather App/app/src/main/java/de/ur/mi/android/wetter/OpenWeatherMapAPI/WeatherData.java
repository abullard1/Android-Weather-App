package de.ur.mi.android.wetter.OpenWeatherMapAPI;

/**
 * This class represents the weather information that was retrieved from the HTTP reqeust to the Weather API.
 */
public class WeatherData {
    public final String city; // City name;
    public final String description; // Description of the current weather in city.
    public final String temperature; // Temperature in city.

    public WeatherData(String city, String description, String temperature) {
        this.city = city;
        this.description = description;
        this.temperature = temperature;
    }

}