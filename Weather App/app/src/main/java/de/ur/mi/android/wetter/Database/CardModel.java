package de.ur.mi.android.wetter.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Not used!
 **/
@Entity(tableName = "CityCard")
public class CardModel {

    //Primary key
    @PrimaryKey(autoGenerate = true)
    private int key;

    @ColumnInfo(name = "cityName")
    private String cityName;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "temperature")
    private String temperature;


    //Getters
    public int getKey() {
        return key;
    }

    //Setters
    public void setKey(int key) {
        this.key = key;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

}
