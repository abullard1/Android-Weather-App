package de.ur.mi.android.wetter;

import android.graphics.Bitmap;

/**Class representing a cityWeatherCard**/
public class CityWeatherCardItem {
    String cityName; //City name
    String description; //Description of the weather in that city
    String temperature; //Temperature in that city (Degrees Celsius)
    Bitmap cityImage; //Image of the city

    public CityWeatherCardItem(String cityName, String description, String temperature) {
        this.cityName = cityName;
        this.description = description;
        this.temperature = temperature;
        this.cityImage = null;
    }

    //Getters
    public String getCityName() {
        return cityName;
    }

    public String getDescription() {
        return description;
    }

    public String getTemperature() {
        return temperature;
    }

    public Bitmap getCityImage() {
        return cityImage;
    }

    //Setters
    public void setDescription(String description) {
        this.description = description;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setCityImage(Bitmap cityImage) {
        this.cityImage = cityImage;
    }
}
